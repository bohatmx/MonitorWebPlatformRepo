/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boha.monitor.utilx;

import com.boha.monitor.data.City;
import com.boha.monitor.data.Company;
import com.boha.monitor.data.GcmDevice;
import com.boha.monitor.data.LocationTracker;
import com.boha.monitor.data.Monitor;
import com.boha.monitor.data.MonitorProject;
import com.boha.monitor.data.MonitorTrade;
import com.boha.monitor.data.PhotoUpload;
import com.boha.monitor.data.Portfolio;
import com.boha.monitor.data.Programme;
import com.boha.monitor.data.Project;
import com.boha.monitor.data.ProjectStatusType;
import com.boha.monitor.data.ProjectTask;
import com.boha.monitor.data.ProjectTaskStatus;
import com.boha.monitor.data.Staff;
import com.boha.monitor.data.StaffProject;
import com.boha.monitor.data.SubTask;
import com.boha.monitor.data.Task;
import com.boha.monitor.data.TaskStatusType;
import com.boha.monitor.data.TaskType;
import com.boha.monitor.data.VideoUpload;
import com.boha.monitor.dto.CompanyDTO;

import com.boha.monitor.dto.GcmDeviceDTO;
import com.boha.monitor.dto.LocationTrackerDTO;
import com.boha.monitor.dto.MonitorDTO;
import com.boha.monitor.dto.MonitorProjectDTO;
import com.boha.monitor.dto.MonitorTradeDTO;
import com.boha.monitor.dto.PhotoUploadDTO;
import com.boha.monitor.dto.PortfolioDTO;
import com.boha.monitor.dto.ProgrammeDTO;
import com.boha.monitor.dto.ProjectDTO;
import com.boha.monitor.dto.ProjectStatusTypeDTO;
import com.boha.monitor.dto.ProjectTaskDTO;
import com.boha.monitor.dto.ProjectTaskStatusDTO;
import com.boha.monitor.dto.StaffDTO;
import com.boha.monitor.dto.StaffProjectDTO;
import com.boha.monitor.dto.SubTaskDTO;
import com.boha.monitor.dto.TaskDTO;
import com.boha.monitor.dto.TaskStatusTypeDTO;
import com.boha.monitor.dto.VideoUploadDTO;
import com.boha.monitor.dto.transfer.ResponseDTO;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.joda.time.DateTime;

/**
 * Data utility class contains methods to add data to the database
 *
 * @author aubreyM
 */
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class DataUtil {

    @PersistenceContext
    EntityManager em;

    final int OPERATIONS_MANAGER = 1,
            SITE_SUPERVISOR = 2,
            EXECUTIVE_STAFF = 3,
            PROJECT_MANAGER = 4;

    public EntityManager getEntityManager() {
        return em;
    }

    public ResponseDTO updateDevice(String oldReg, String canonicalReg) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        try {
            Query q = em.createNamedQuery("GcmDevice.findByRegistrationID", GcmDevice.class);
            q.setParameter("registrationID", oldReg);
            List<GcmDevice> list = q.getResultList();
            if (!list.isEmpty()) {
                GcmDevice dev = list.get(0);
                em.merge(dev);
                log.log(Level.SEVERE, "gcmDevice registrationID updated with canonical");
            }
            
            
        } catch (Exception e) {
            throw new DataException("Failed");
        }
        
        return resp;
    }
    /**
     * Assign tasks to project using taskType. ie, assign all tasks in this
     * taskType to this project
     *
     * @param projectID
     * @return
     * @throws DataException
     */
    public ResponseDTO addProjectTasksUsingCompany(Integer projectID) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        try {

            Project p = em.find(Project.class, projectID);
            Query q = em.createNamedQuery("Task.findByCompany", Task.class);
            q.setParameter("companyID", p.getCompany().getCompanyID());
            List<Task> taskList = q.getResultList();
            for (Task t : taskList) {

                ProjectTask pt = new ProjectTask();
                pt.setDateRegistered(new Date());
                pt.setProject(p);
                pt.setTask(t);
                em.persist(pt);
                log.log(Level.INFO, "ProjectTask added: {0} - {1}", new Object[]{p.getProjectName(), t.getTaskName()});
            }
            em.flush();
            q = em.createNamedQuery("ProjectTask.findByProject", ProjectTask.class);
            q.setParameter("projectID", p.getProjectID());
            List<ProjectTask> pList = q.getResultList();
            resp.setProjectTaskList(new ArrayList<>());
            for (ProjectTask projectTask : pList) {
                resp.getProjectTaskList().add(new ProjectTaskDTO(projectTask));
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, "", e);
            throw new DataException("Failed to add project tasks");
        }
        return resp;

    }

    /**
     * Assign tasks to project
     *
     * @param list
     * @return list of all project tasks
     * @throws DataException
     */
    public ResponseDTO addProjectTasks(List<ProjectTaskDTO> list) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        try {
            Project p = em.find(Project.class, list.get(0).getProjectID());
            for (ProjectTaskDTO dto : list) {
                if (dto.getProjectTaskID() != null) {
                    ProjectTask pt = em.find(ProjectTask.class, dto.getProjectTaskID());
                    if (!dto.getTask().isSelected()) {
                        em.remove(pt);
                        em.flush();
                        log.log(Level.INFO, "Removed ProjectTask: {0} from {1}",
                                new Object[]{dto.getTask().getTaskName(), p.getProjectName()});
                    }

                } else if (dto.getTask().isSelected()) {
                    Query q = em.createNamedQuery("ProjectTask.findByProjectTask", ProjectTask.class);
                    q.setParameter("projectID", p.getProjectID());
                    q.setParameter("taskID", dto.getTask().getTaskID());
                    List<ProjectTask> ptList = q.getResultList();
                    if (ptList.isEmpty()) {

                        Task t = em.find(Task.class, dto.getTask().getTaskID());
                        ProjectTask pt = new ProjectTask();
                        pt.setDateRegistered(new Date());
                        pt.setProject(p);
                        pt.setTask(t);
                        em.persist(pt);
                        log.log(Level.INFO, "ProjectTask added: {0} - {1}",
                                new Object[]{p.getProjectName(), t.getTaskName()});
                    }
                }

            }

            em.flush();
            Query q = em.createNamedQuery("ProjectTask.findByProject", ProjectTask.class);
            q.setParameter("projectID", p.getProjectID());
            List<ProjectTask> pList = q.getResultList();
            resp.setProjectTaskList(new ArrayList<>());
            for (ProjectTask projectTask : pList) {
                resp.getProjectTaskList().add(new ProjectTaskDTO(projectTask));
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, "", e);
            throw new DataException("Failed to add project tasks");
        }
        return resp;

    }

    /**
     * Add tasks to company
     *
     * @param list
     * @return
     * @throws DataException
     */
    public ResponseDTO addTasks(List<TaskDTO> list) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        Company company = em.find(Company.class, list.get(0).getCompanyID());
        List<Task> taskList = new ArrayList<>(20);
        try {
            for (TaskDTO taskDTO : list) {
                Task t = new Task();
                t.setCompany(company);
                t.setDescription(taskDTO.getDescription());
                t.setTaskName(taskDTO.getTaskName());
                t.setTaskNumber(taskDTO.getTaskNumber());
                em.persist(t);
                em.flush();
                taskList.add(t);

            }
            resp.setTaskList(new ArrayList<>());
            for (Task task : taskList) {
                resp.getTaskList().add(new TaskDTO(task));
            }
            log.log(Level.INFO, "Tasks added: {0}", new Object[]{
                resp.getTaskList().size()});

        } catch (Exception e) {
            log.log(Level.SEVERE, "", e);
            throw new DataException("Failed to set project tasks");
        }
        return resp;
    }

    public ResponseDTO addSubTasks(List<SubTaskDTO> list) throws DataException {
        ResponseDTO resp = new ResponseDTO();

        try {
            Task tt = em.find(Task.class, list.get(0).getTaskID());
            for (SubTaskDTO taskDTO : list) {
                SubTask t = new SubTask();
                t.setTask(tt);
                t.setDescription(taskDTO.getDescription());
                t.setSubTaskName(taskDTO.getSubTaskName());
                t.setSubTaskNumber(taskDTO.getSubTaskNumber());

                em.persist(t);

            }
            em.flush();
            Query q = em.createNamedQuery("SubTask.findByTask", SubTask.class
            );
            q.setParameter("taskID", tt.getTaskID());
            List<SubTask> taskList = q.getResultList();
            resp.setSubTaskList(new ArrayList<>());
            for (SubTask task : taskList) {
                resp.getSubTaskList().add(new SubTaskDTO(task));
            }
            log.log(Level.INFO, "SubTasks added: {0} - {1}", new Object[]{tt.getTaskName(),
                resp.getTaskList().size()});

        } catch (Exception e) {
            log.log(Level.SEVERE, "", e);
            throw new DataException("Failed to add subtasks");
        }
        return resp;
    }

    public ResponseDTO importProject(ProjectDTO dto) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        resp.setProjectList(new ArrayList<>());
        log.log(Level.INFO, "#### Projects import started ....########");
        try {
            Project p = new Project();

            if (dto.getProgrammeID() != null) {
                p.setProgramme(em.find(Programme.class, dto.getProgrammeID()));

            }
            if (dto.getCityID() != null) {
                p.setCity(em.find(City.class, dto.getCityID()));
            }
            p.setProjectName(dto.getProjectName());
            p.setDateRegistered(new Date());

            em.persist(p);
            em.flush();
            log.log(Level.INFO, "### Project added: {0}", dto.getProjectName());
            //assign all programme tasks to projects
            Query q0 = em.createNamedQuery("Task.findByCompany", Task.class
            );
            q0.setParameter("companyID", dto.getCompanyID());
            List<Task> taskList = q0.getResultList();
            ProjectDTO projectDTO = new ProjectDTO(p);
            projectDTO.setProjectTaskList(new ArrayList<>());

            for (Task task : taskList) {
                ProjectTask pt = new ProjectTask();
                pt.setProject(p);
                pt.setTask(task);
                pt.setDateRegistered(new Date());
                em.persist(pt);
                em.flush();
                ProjectTaskDTO ptd = new ProjectTaskDTO(pt);
                projectDTO.getProjectTaskList().add(ptd);
                log.log(Level.OFF, "ProjectTask added to project:  {0} task: {1}",
                        new Object[]{projectDTO.getProjectName(), task.getTaskName()});

            }

        } catch (Exception e) {
            log.log(Level.SEVERE, "failed", e);
            throw new DataException("Failed to import project");
        }
        return resp;
    }

    public ResponseDTO addMonitorTrades(List<MonitorTradeDTO> list) throws DataException {
        ResponseDTO resp = new ResponseDTO();

        try {
            Monitor p = em.find(Monitor.class, list.get(0).getMonitorID());
            for (MonitorTradeDTO d : list) {
                MonitorTrade t = new MonitorTrade();
                t.setDateRegistered(new Date());
                t.setMonitor(p);
//                t.setTaskType(em.find(TaskType.class, d.getTaskTypeID()));

                em.persist(t);

            }

            Query q = em.createNamedQuery("MonitorTrade.findByMonitor", MonitorTrade.class
            );
            q.setParameter(
                    "monitorID", p.getMonitorID());
            List<MonitorTrade> ttList = q.getResultList();

            resp.setMonitorTradeList(new ArrayList<>());
            for (MonitorTrade mt : ttList) {
                resp.getMonitorTradeList().add(new MonitorTradeDTO(mt));
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, "", e);
            throw new DataException("Failed to add MonitorTrades");
        }
        return resp;
    }

    /**
     * Assign projects to Staff member
     *
     * @param list
     * @return
     * @throws DataException
     */
    public ResponseDTO addStaffProjects(List<StaffProjectDTO> list) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        Staff cs = em.find(Staff.class, list.get(0).getStaffID());

        Query q = em.createNamedQuery("StaffProject.findStaffProjects", StaffProject.class
        );
        q.setParameter("staffID", list.get(0).getStaffID());
        List<StaffProject> spList = q.getResultList();
        for (StaffProject sp : spList) {
            em.remove(sp);
        }
        em.flush();
        log.log(Level.INFO, "StaffProjects removed: {0}", spList.size());
        try {
            for (StaffProjectDTO sp : list) {
                StaffProject d = new StaffProject();
                d.setStaff(cs);
                d
                        .setProject(em.find(Project.class, sp.getProjectID()));
                d.setActiveFlag(true);
                d.setDateAssigned(new Date());
                em.persist(d);
            }
            em.flush();

            q
                    = em.createNamedQuery("StaffProject.findByStaff", StaffProject.class
                    );
            q.setParameter("staffID", list.get(0).getStaffID());
            List<StaffProject> sList = q.getResultList();
            resp.setStaffProjectList(new ArrayList<>());
            for (StaffProject x : sList) {
                resp.getStaffProjectList().add(new StaffProjectDTO(x));
            }
            log.log(Level.INFO, "Staff projects assigned: {0}", resp.getStaffProjectList().size());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to add Staff projects ", e);
            throw new DataException("Failed to add Staff projects \n"
                    + getErrorString(e));
        }
        return resp;
    }

    /**
     * Assign projects to Monitor
     *
     * @param list
     * @return
     * @throws DataException
     */
    public ResponseDTO addMonitorProjects(List<MonitorProjectDTO> list) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        Monitor cs = em.find(Monitor.class, list.get(0).getMonitorID());
        Query q = em.createNamedQuery("MonitorProject.findMonitorProjects", MonitorProject.class
        );
        q.setParameter("monitorID", list.get(0).getMonitorID());
        List<MonitorProject> spList = q.getResultList();
        for (MonitorProject sp : spList) {
            em.remove(sp);
        }
        em.flush();
        log.log(Level.INFO, "MonitorProjects removed: {0}", spList.size());
        try {
            for (MonitorProjectDTO sp : list) {
                MonitorProject d = new MonitorProject();
                d.setMonitor(cs);
                d
                        .setProject(em.find(Project.class, sp.getProjectID()));
                d.setActiveFlag(true);
                d.setDateAssigned(new Date());
                em.persist(d);
            }
            em.flush();
            q
                    = em.createNamedQuery("MonitorProject.findMonitorProjects", MonitorProject.class
                    );
            q.setParameter("monitorID", list.get(0).getMonitorID());
            List<MonitorProject> sList = q.getResultList();
            resp.setMonitorProjectList(new ArrayList<>());
            for (MonitorProject x : sList) {
                resp.getMonitorProjectList().add(new MonitorProjectDTO(x));
            }
            log.log(Level.INFO, "Monitor projects assigned: {0}", resp.getMonitorProjectList().size());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to add Monitor projects ", e);
            throw new DataException("Failed to add Monitor projects \n"
                    + getErrorString(e));
        }
        return resp;
    }

    public Company
            getCompanyByID(Integer id) {
        return em.find(Company.class, id);
    }

    public Staff
            getStaffByID(Integer id) {
        return em.find(Staff.class, id);
    }

    public ResponseDTO updatePhotoUpload(PhotoUploadDTO pu) {
        ResponseDTO resp = new ResponseDTO();
        try {
            PhotoUpload photo = em.find(PhotoUpload.class, pu.getPhotoUploadID());
            photo.setMarked(pu.getMarked());
            if (photo.getSharedCount() == null) {
                photo.setSharedCount(pu.getSharedCount());
            } else {
                photo.setSharedCount(photo.getSharedCount() + pu.getSharedCount());
            }
            em.merge(photo);
            log.log(Level.INFO, "Photo has been updated");
            resp.setMessage("Photo update, marked: " + pu.getMarked() + " shared: " + photo.getSharedCount());

        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return resp;
    }

    public ResponseDTO addPhotoUpload(PhotoUploadDTO pu) {
        ResponseDTO w = new ResponseDTO();
        w.setPhotoUploadList(new ArrayList<>());
        try {
            PhotoUpload u = new PhotoUpload();

            if (pu.getProjectTaskStatus() != null) {
                u.setProjectTaskStatus(em.find(ProjectTaskStatus.class, pu.getProjectTaskStatus().getProjectTaskStatusID()));

            }

            if (pu.getProjectID()
                    != null) {
                u.setProject(em.find(Project.class, pu.getProjectID()));

            }

            if (pu.getProjectTaskID()
                    != null) {
                u.setProjectTask(em.find(ProjectTask.class, pu.getProjectTaskID()));

            }

            if (pu.getStaffID()
                    != null) {
                u.setStaff(em.find(Staff.class, pu.getStaffID()));

            }

            if (pu.getMonitorID()
                    != null) {
                u.setMonitor(em.find(Monitor.class, pu.getMonitorID()));
            }

            u.setPictureType(pu.getPictureType());
            u.setLatitude(pu.getLatitude());
            u.setLongitude(pu.getLongitude());
            u.setUri(pu.getUri());
            u.setDateTaken(
                    new Date(pu.getDateTaken()));
            u.setDateUploaded(
                    new Date(pu.getDateUploaded()));
            u.setThumbFlag(pu.getThumbFlag());
            u.setAccuracy(pu.getAccuracy());

            u.setSecureUrl(pu.getSecureUrl());
            u.setBytes(pu.getBytes());
            u.setHeight(pu.getHeight());
            u.setWidth(pu.getWidth());

            em.persist(u);
            em.flush();
            w.getPhotoUploadList().add(new PhotoUploadDTO(u));

            log.log(Level.OFF,
                    "PhotoUpload added to table, date taken: {0}", u.getDateTaken().toString());
        } catch (Exception e) {
            log.log(Level.SEVERE, "PhotoUpload failed", e);
            
        }
        return w;
    }

    public ResponseDTO generateMonitorPin(Integer monitorID) throws DataException {
        ResponseDTO resp = new ResponseDTO();

        try {
            Monitor mon = em.find(Monitor.class, monitorID);
            if (mon != null) {
                mon.setPin(getRandomPin());
                em.merge(mon);
                resp.setMonitor(new MonitorDTO(mon));
                resp.setMessage("New Monitor PIN generated");
                log.log(Level.INFO, "Monitor PIN generated");
            }
        } catch (Exception e) {
            log.log(Level.OFF, null, e);
            throw new DataException("Failed to update staff\n" + getErrorString(e));
        }

        return resp;
    }

    public ResponseDTO generateStaffPin(Integer staffID) throws DataException {
        ResponseDTO resp = new ResponseDTO();

        try {
            Staff staff = em.find(Staff.class, staffID);
            if (staff != null) {
                staff.setPin(getRandomPin());
                em.merge(staff);
                resp.setStaff(new StaffDTO(staff));
                resp.setMessage("New Staff PIN generated");
                log.log(Level.INFO, "Staff PIN generated");
            }
        } catch (Exception e) {
            log.log(Level.OFF, null, e);
            throw new DataException("Failed to update staff\n" + getErrorString(e));
        }

        return resp;
    }

    public ResponseDTO updateStaff(StaffDTO dto) throws DataException {
        ResponseDTO resp = new ResponseDTO();

        try {
            Staff ps = em.find(Staff.class, dto.getStaffID());
            if (ps != null) {
                if (dto.getFirstName() != null) {
                    ps.setFirstName(dto.getFirstName());
                }
                if (dto.getLastName() != null) {
                    ps.setLastName(dto.getLastName());
                }
                if (dto.getActiveFlag() != null) {
                    ps.setActiveFlag(dto.getActiveFlag());
                }
                if (dto.getEmail() != null) {
                    ps.setEmail(dto.getEmail());
                }
                if (dto.getCellphone() != null) {
                    ps.setCellphone(dto.getCellphone());
                }
                if (dto.getAppInvitationDate() != null) {
                    ps.setAppInvitationDate(new Date());
                }

                em.merge(ps);
                resp.setStaffList(new ArrayList<>());
                resp.getStaffList().add(new StaffDTO(ps));
                log.log(Level.INFO, "Staff updated");
            }
        } catch (Exception e) {
            log.log(Level.OFF, null, e);
            throw new DataException("Failed to update staff\n" + getErrorString(e));
        }

        return resp;
    }

    public ResponseDTO addVideoUpload(VideoUploadDTO pu) {
        ResponseDTO w = new ResponseDTO();
        w.setPhotoUploadList(new ArrayList<>());
        try {
            VideoUpload u = new VideoUpload();

            if (pu.getProjectID()
                    != null) {
                u.setProject(em.find(Project.class, pu.getProjectID()));

            }

            if (pu.getProjectTaskID()
                    != null) {
                u.setProjectTask(em.find(ProjectTask.class, pu.getProjectTaskID()));

            }

            if (pu.getStaffID()
                    != null) {
                u.setStaff(em.find(Staff.class, pu.getStaffID()));

            }

            if (pu.getMonitorID()
                    != null) {
                u.setMonitor(em.find(Monitor.class, pu.getMonitorID()));
            }
            u.setYouTubeID(pu.getYouTubeID());
            u.setLatitude(pu.getLatitude());
            u.setLongitude(pu.getLongitude());
            u.setUrl(pu.getUrl());
            u.setDateTaken(
                    new Date(pu.getDateTaken()));
            u.setDateUploaded(
                    new Date(pu.getDateUploaded()));
            u.setAccuracy(pu.getAccuracy());

            u.setSecureUrl(pu.getSecureUrl());
            u.setETag(pu.geteTag());
            u.setBytes(pu.getBytes());
            u.setSignature(pu.getSignature());
            u.setHeight(pu.getHeight());
            u.setWidth(pu.getWidth());

            em.persist(u);
            em.flush();
            w.getVideoUploadList().add(new VideoUploadDTO(u));

            log.log(Level.OFF,
                    "VideoUpload added to table, date taken: {0}", u.getDateTaken().toString());
        } catch (Exception e) {
            log.log(Level.SEVERE, "VideoUpload failed", e);
           
        }
        return w;
    }

    public ResponseDTO deleteProjectPhotos(List<PhotoUploadDTO> list) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        int count = 0;

        try {
            for (PhotoUploadDTO p : list) {
                PhotoUpload u = em.find(PhotoUpload.class, p.getPhotoUploadID());
                em.remove(u);

                count++;
            }
            em.flush();
            resp.setStatusCode(ServerStatus.OK);
            resp.setMessage("" + count + " project photos deleted");
            log.log(Level.WARNING, "photos deleted: {0}", count);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed delete photo\n" + getErrorString(e));

        }
        return resp;
    }

    public void addLocationTrack(LocationTrackerDTO dto) throws DataException {
        try {
            LocationTracker t = new LocationTracker();

            if (dto.getStaffID() != null) {
                t.setStaff(em.find(Staff.class, dto.getStaffID()));
            }
            t.setDateTracked(
                    new Date(dto.getDateTracked()));
            t.setLatitude(dto.getLatitude());
            t.setLongitude(dto.getLongitude());
            t.setAccuracy(dto.getAccuracy());
            t.setDateAdded(
                    new Date());
            t.setDateTrackedLong(BigInteger.valueOf(dto.getDateTracked()));
            t.setGeocodedAddress(dto.getGeocodedAddress());

            if (dto.getGcmDevice() != null) {
                if (dto.getGcmDevice().getGcmDeviceID() != null) {
                    t.setGcmDevice(em.find(GcmDevice.class, dto.getGcmDevice().getGcmDeviceID()));

                }
            } else {
                throw new DataException("");
            }
            if (dto.getMonitorID() != null) {
                Monitor m = em.find(Monitor.class, dto.getMonitorID());
                t.setMonitor(m);
                t
                        .setCompany(em.find(Company.class, m.getCompany().getCompanyID()));

            }
            if (dto.getStaffID() != null) {
                Staff m = em.find(Staff.class, dto.getStaffID());
                t.setStaff(m);
                t
                        .setCompany(em.find(Company.class, m.getCompany().getCompanyID()));
            }
            em.persist(t);

            log.log(Level.WARNING, "LocationTrack added");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed to add device\n" + getErrorString(e));

        }
    }

    public void addLocationTrackers(List<LocationTrackerDTO> list) throws DataException {
        try {
            for (LocationTrackerDTO dto : list) {
                addLocationTrack(dto);
            }

            log.log(Level.WARNING, "LocationTrackers added: {0}", list.size());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed to add device\n" + getErrorString(e));

        }
    }

    public void addDevice(GcmDeviceDTO d) throws DataException {

        try {
            GcmDevice g = new GcmDevice();
            g
                    .setCompany(em.find(Company.class, d.getCompanyID()));

            if (d.getStaffID()
                    != null) {
                g.setStaff(em.find(Staff.class, d.getStaffID()));

            }

            if (d.getMonitorID()
                    != null) {
                g.setMonitor(em.find(Monitor.class, d.getMonitorID()));
            }

            g.setDateRegistered(
                    new Date());
            g.setManufacturer(d.getManufacturer());
            g.setMessageCount(
                    0);
            g.setModel(d.getModel());
            g.setRegistrationID(d.getRegistrationID());
            g.setSerialNumber(d.getSerialNumber());
            g.setProduct(d.getProduct());
            g.setAndroidVersion(d.getAndroidVersion());

            em.persist(g);

            log.log(Level.WARNING,
                    "New device loaded");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed to add device\n" + getErrorString(e));

        }
    }

    public ResponseDTO addProjectTaskStatus(
            ProjectTaskStatusDTO status) throws DataException {
        ResponseDTO resp = new ResponseDTO();

        try {
            Query q = em.createNamedQuery("ProjectTaskStatus.findByTaskStatusDate",
                    ProjectTaskStatus.class);
            q.setParameter("projectTaskID", status.getProjectTaskID());
            q.setParameter("statusDate", new Date(status.getStatusDate()));
            List<ProjectTaskStatus> list = q.getResultList();
            if (!list.isEmpty()) {
                resp.setStatusCode(StatusCode.ERROR_DATABASE);
                resp.setMessage("This project status is a duplicate of another already on the system");
                log.log(Level.WARNING, "ProjectTaskStatus is a DUPLICATE, ignored");
                return resp;

            }
            ProjectTask c = em.find(ProjectTask.class,
                    status.getProjectTaskID());
            ProjectTaskStatus t = new ProjectTaskStatus();
            t.setDateUpdated(new Date());
            if (status.getStatusDate() != null) {
                t.setStatusDate(new Date(status.getStatusDate()));
            } else {
                t.setStatusDate(new Date());
            }

            t.setProjectTask(c);
            t.setTaskStatusType(em.find(TaskStatusType.class,
                    status.getTaskStatusType().getTaskStatusTypeID()));

            if (status.getStaffID() != null) {
                t.setStaff(em.find(Staff.class, status.getStaffID()));

            }

            if (status.getMonitorID() != null) {
                t.setMonitor(em.find(Monitor.class, status.getMonitorID()));
            }

            em.persist(t);
            em.flush();

            resp.setProjectTaskStatusList(
                    new ArrayList<>());
            resp.getProjectTaskStatusList()
                    .add(new ProjectTaskStatusDTO(t));
            log.log(Level.OFF, "ProjectTaskStatus added: {0} type: {1}", new Object[]{c.getTask().getTaskName(), t.getTaskStatusType().getTaskStatusTypeName()});
        } catch (PersistenceException e) {
            resp.setStatusCode(StatusCode.ERROR_DATABASE);
            resp.setMessage("This project status is a duplicate of another already on the system");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed\n" + getErrorString(e));
        }

        return resp;

    }

    /**
     * Add TaskStatusTypes to database for the Company
     *
     * @param taskStatusTypeList
     * @return
     * @throws DataException
     */
    public ResponseDTO addTaskStatusTypes(
            List<TaskStatusTypeDTO> taskStatusTypeList) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        Company c = em.find(Company.class,
                taskStatusTypeList.get(0).getCompanyID());

        try {
            for (TaskStatusTypeDTO x : taskStatusTypeList) {
                TaskStatusType t = new TaskStatusType();
                t.setCompany(c);
                t.setStatusColor(x.getStatusColor());
                t.setTaskStatusTypeName(x.getTaskStatusTypeName());
                em.persist(t);
            }

            em.flush();
            Query q = em.createNamedQuery("TaskStatusType.findByCompany", TaskStatusType.class
            );
            q.setParameter("companyID", c.getCompanyID());
            List<TaskStatusType> mList = q.getResultList();

            resp.setTaskStatusTypeList(new ArrayList<>());
            for (TaskStatusType tt : mList) {
                resp.getTaskStatusTypeList().add(
                        new TaskStatusTypeDTO(tt));
            }

            log.log(Level.OFF, "TaskStatusTypes added: {0}", resp.getTaskStatusTypeList().size());

        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed\n" + getErrorString(e));
        }

        return resp;

    }

    public ResponseDTO addProjectStatusTypes(List<ProjectStatusTypeDTO> psList) throws DataException {
        ResponseDTO resp = new ResponseDTO();

        try {
            Company c = em.find(Company.class, psList.get(0).getCompanyID());
            for (ProjectStatusTypeDTO pst : psList) {
                ProjectStatusType cli = new ProjectStatusType();
                cli.setProjectStatusName(pst.getProjectStatusName());
                cli.setStatusColor(pst.getStatusColor());
                cli.setCompany(c);
                cli.setRating(pst.getRating());
                em.persist(cli);
            }

            em.flush();
            Query q = em.createNamedQuery("ProjectStatusType.findByCompany", ProjectStatusType.class
            );

            q.setParameter(
                    "companyID", c.getCompanyID());
            List<ProjectStatusType> xList = q.getResultList();

            resp.setProjectStatusTypeList(
                    new ArrayList<>());
            for (ProjectStatusType x : xList) {
                resp.getProjectStatusTypeList().add(new ProjectStatusTypeDTO(x));
            }

            log.log(Level.OFF,
                    "######## ProjectStatusTypes added: {0}", resp.getProjectStatusTypeList().size());

        } catch (PersistenceException e) {
            log.log(Level.SEVERE, "Failed", e);
            resp.setStatusCode(ServerStatus.ERROR_DUPLICATE_DATA);
            resp.setMessage(ServerStatus.getMessage(resp.getStatusCode()));

        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed\n" + getErrorString(e));
        }
        return resp;
    }

    /**
     * Assign Task to a Project
     *
     * @param projectTask
     * @return
     * @throws DataException
     */
    public ResponseDTO addProjectTask(
            ProjectTaskDTO projectTask) throws DataException {
        ResponseDTO resp = new ResponseDTO();

        try {
            Project site = em.find(Project.class, projectTask.getProjectID());
            Task task = em.find(Task.class, projectTask.getTask().getTaskID());
            ProjectTask t = new ProjectTask();

            t.setDateRegistered(
                    new Date());
            t.setProject(site);

            t.setTask(task);

            em.persist(t);

            em.flush();

            resp.setProjectTaskList(
                    new ArrayList<>());
            resp.getProjectTaskList()
                    .add(new ProjectTaskDTO(t));
            resp.setStatusCode(
                    0);
            resp.setMessage(
                    "ProjectTask added successfully");
            log.log(Level.OFF,
                    "Project task added for: {0} ",
                    new Object[]{site.getProjectName()
                    }
            );

        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed\n" + getErrorString(e));
        }

        return resp;

    }

    /**
     * Add projects to a Company or Programme
     *
     * @param projectList
     * @return
     * @throws DataException
     */
    public ResponseDTO addProjects(
            List<ProjectDTO> projectList) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        Company c = null;
        Programme p = null;

        try {
            c = em.find(Company.class, projectList.get(0).getCompanyID());

            if (projectList.get(0).getProgrammeID() != null) {
                p = em.find(Programme.class, projectList.get(0).getProgrammeID());
            }

            for (ProjectDTO dto : projectList) {

                Project ps = new Project();
                ps.setProgramme(p);
                ps.setCompany(c);
                ps.setDescription(dto.getDescription());
                ps.setAccuracy(dto.getAccuracy());
                ps.setAddress(dto.getAddress());
                ps.setActiveFlag(Boolean.TRUE);
                ps.setLatitude(dto.getLatitude());
                ps.setLongitude(dto.getLongitude());
                ps.setProjectName(dto.getProjectName());
                ps.setDateRegistered(new Date());

                em.persist(ps);
            }
            em.flush();

            List<String> sList = new ArrayList<>();
            for (ProjectDTO pr : projectList) {
                sList.add(pr.getProjectName());

            }
            Query q = em.createNamedQuery("Project.findByCompanyProjectNames", Project.class
            );
            q.setParameter("companyID", c.getCompanyID());
            q.setParameter("list", sList);
            List<Project> prList = q.getResultList();
            for (Project project : prList) {
                resp.getProjectList().add(new ProjectDTO(project));
            }
            log.log(Level.OFF,
                    "Projects  registered for: {0} - {1} ",
                    new Object[]{c.getCompanyName(), resp.getProjectList().size()
                    }
            );

        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed\n" + getErrorString(e));
        }

        return resp;

    }

    public ResponseDTO addPortfolios(
            List<PortfolioDTO> portfolioList) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        Company c = null;
        Programme p = null;

        try {
            if (portfolioList.get(0).getCompanyID() != null) {
                c = em.find(Company.class, portfolioList.get(0).getCompanyID());
            }

            for (PortfolioDTO dto : portfolioList) {
                Portfolio ps = new Portfolio();
                ps.setCompany(c);
                ps.setPortfolioName(dto.getPortfolioName());
                ps.setDateRegistered(new Date());
                em.persist(ps);
            }
            //em.flush();

            resp = ListUtil.getCompanyData(em, c.getCompanyID());
            log.log(Level.OFF,
                    "Portfolios  registered for: {0} - {1} ",
                    new Object[]{c.getCompanyName(), resp.getPortfolioList().size()
                    }
            );

        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed\n" + getErrorString(e));
        }

        return resp;

    }

    public ResponseDTO addProgrammes(
            List<ProgrammeDTO> programmeList) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        Portfolio portfolio = null;

        try {
            portfolio = em.find(Portfolio.class, programmeList.get(0).getPortfolioID());
            if (portfolio == null) {
                throw new DataException("Portfolio not found for programme add");
            }

            for (ProgrammeDTO dto : programmeList) {
                Programme ps = new Programme();
                ps.setPortfolio(portfolio);
                ps.setProgrammeName(dto.getProgrammeName());
                ps.setDateRegistered(new Date());
                ps.setDescription(dto.getDescription());
                em.persist(ps);
            }
            //em.flush();
            resp = ListUtil.getCompanyData(em, portfolio.getCompany().getCompanyID());
            log.log(Level.OFF,
                    "Programmes  registered for: {0} - {1} ",
                    new Object[]{portfolio.getPortfolioName(), resp.getProgrammeList().size()
                    }
            );

        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed\n" + getErrorString(e));
        }

        return resp;

    }


    /**
     * Add staff members to company
     *
     * @param staffList
     * @return list of all company staff
     * @throws DataException
     */
    public ResponseDTO addStaffList(
            List<StaffDTO> staffList) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        resp.setStaffList(new ArrayList<>());

        try {
            Company c = em.find(Company.class, staffList.get(0).getCompanyID());
            resp.setCompany(new CompanyDTO(c));
            for (StaffDTO staff : staffList) {
                resp.getStaffList().add(addStaff(staff, c));
            }
        } catch (PersistenceException e) {
            resp.setStatusCode(ServerStatus.ERROR_DUPLICATE_DATA);
            resp.setMessage(ServerStatus.getMessage(resp.getStatusCode()));
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed to register staff\n");
        }

        return resp;

    }

    private StaffDTO addStaff(StaffDTO staff, Company c) throws DataException {
        StaffDTO s = new StaffDTO();

        try {
            Staff staffX = new Staff();
            staffX.setCompany(c);
            staffX.setFirstName(staff.getFirstName());
            staffX.setCellphone(staff.getCellphone());
            staffX.setEmail(staff.getEmail());
            staffX.setLastName(staff.getLastName());
            staffX.setPin(getRandomPin());
            staffX.setActiveFlag(staff.getActiveFlag());
            staffX.setDateRegistered(new Date());

            Query q = em.createNamedQuery("Staff.findByEmail", Staff.class
            );
            q.setParameter("email", staff.getEmail());
            List<Staff> stx = q.getResultList();
            if (stx.isEmpty()) {
                em.persist(staffX);
                em.flush();
                s = new StaffDTO(staffX);
                log.log(Level.OFF,
                        "Staff registered for: {0} - {1} ",
                        new Object[]{c.getCompanyName(), s.getFirstName()
                        });
            } else {
                throw new DataException("Email account already exists");
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, "Staff add failed", e);
            throw new DataException("Staff add failed");
        }
        return s;
    }

    /**
     * Add Monitors to company
     *
     * @param monitorList
     * @return list of all company monitors
     * @throws DataException
     */
    public ResponseDTO addMonitors(
            List<MonitorDTO> monitorList) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        resp.setMonitorList(new ArrayList<>());

        Company c = em.find(Company.class, monitorList.get(0).getCompanyID());
        for (MonitorDTO mon : monitorList) {
            resp.getMonitorList().add(addMonitor(mon, c));
        }

        return resp;

    }

    /**
     * Register company monitor
     *
     * @param mon
     * @param c
     * @return
     * @throws DataException
     */
    private MonitorDTO addMonitor(
            MonitorDTO mon, Company c) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        Query q = em.createNamedQuery("Monitor.findByEmail", Monitor.class
        );

        MonitorDTO mx = new MonitorDTO();
        try {
            Monitor m = new Monitor();
            m.setCompany(c);
            m.setFirstName(mon.getFirstName());
            m.setCellphone(mon.getCellphone());
            m.setEmail(mon.getEmail());
            m.setLastName(mon.getLastName());
            m.setPin(getRandomPin());
            m.setActiveFlag(mon.getActiveFlag());
            m.setDateRegistered(new Date());
            m.setIDNumber(mon.getIDNumber());
            m.setAddress(mon.getAddress());
            m.setGender(mon.getGender());
            q.setParameter("email", mon.getEmail());
            List<Monitor> mList = q.getResultList();
            if (mList.isEmpty()) {
                em.persist(m);
                em.flush();
                mx = new MonitorDTO(m);

                try {
                    if (mon.getGcmDevice() != null) {
                        addDevice(mon.getGcmDevice());
                    }

                } catch (DataException e) {
                    log.log(Level.WARNING, "Unable to add device to GCMDevice table", e);
                }
            } else {
                throw new DataException("Email already exists");
            }

            log.log(Level.OFF,
                    "Company monitor registered for: {0} - {1} {2}",
                    new Object[]{c.getCompanyName(), mon.getFirstName(), mon.getLastName()
                    }
            );
        } catch (PersistenceException e) {
            resp.setStatusCode(ServerStatus.ERROR_DUPLICATE_DATA);
            resp.setMessage(ServerStatus.getMessage(resp.getStatusCode()));
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed to register monitor\n" + getErrorString(e));
        }

        return mx;

    }

    public ResponseDTO registerCompany(CompanyDTO company) throws DataException {
        log.log(Level.OFF, "####### * attempt to register company");
        ResponseDTO resp = new ResponseDTO();
        try {
            Company c = new Company();
            c.setCompanyName(company.getCompanyName());
            c.setAddress(company.getAddress());
            c.setEmail(company.getEmail());
            c.setCellphone(company.getCellphone());

            em.persist(c);
            em.flush();

            //add initial data - app not empty at startup
            addInitialTaskStatusTypes(c);
            addinitialProjectStatusType(c);
//            addInitialTasks(c);
            addInitialPortfolio(c);

            if (company.getStaffList() != null) {
                addStaffList(company.getStaffList());
            }
            if (company.getMonitorList() != null) {
                addMonitors(company.getMonitorList());
            }

            resp = ListUtil.getCompanyData(em, c.getCompanyID());
            log.log(Level.OFF, "######## Company registered: {0}", c.getCompanyName());

        } catch (PersistenceException e) {
            log.log(Level.SEVERE, "Failed", e);
            resp.setStatusCode(ServerStatus.ERROR_DUPLICATE_DATA);
            resp.setMessage(ServerStatus.getMessage(resp.getStatusCode()));

        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed to register company\n" + getErrorString(e));
        }

        return resp;

    }

    private void addInitialPortfolio(Company c) {
        Portfolio p = new Portfolio();
        p.setCompany(c);
        p.setPortfolioName("General Portfolio (Default)");
        p.setDateRegistered(new Date());

        em.persist(p);
        em.flush();
        log.log(Level.INFO, "#### Initial Portfolio added, ID: {0} - {1}", new Object[]{p.getPortfolioID(), p.getPortfolioName()});

        //add programme
        Programme px = new Programme();
        px.setPortfolio(p);
        px.setDateRegistered(new Date());
        px.setProgrammeName("General Programme (Default)");
        px.setDescription("Default Programme, used where there is only Programme for the organisation");

        em.persist(px);
        em.flush();
        log.log(Level.INFO, "#### Initial Programme added, ID: {0} - {1}", new Object[]{px.getProgrammeID(), px.getProgrammeName()});
        addInitialProject(c, px);
    }

    private void addInitialProject(Company c, Programme p) {

        Project project = new Project();
        project.setProgramme(p);
        project.setDescription("This is a sample project meant to help you practice the features of the Monitor app. "
                + "This project can be edited or removed when you are done");
        project.setProjectName("Sample Project No. 1");
        project.setLatitude(null);
        project.setLongitude(null);
        project.setAddress(null);
        project.setDateRegistered(new Date());

        em.persist(project);
        em.flush();

        log.log(Level.INFO, "#### Initial Project added, ID: {0} - {1}", new Object[]{project.getProjectID(), project.getProjectName()});
        addInitialProjectTasks(c, project);

    }

    private void addInitialProjectTasks(Company c, Project p) {

        Query q = em.createNamedQuery("Task.findByCompany", Task.class
        );
        q.setParameter(
                "companyID", c.getCompanyID());
        List<Task> tList = q.getResultList();
        for (Task task : tList) {
            ProjectTask pt = new ProjectTask();
            pt.setDateRegistered(new Date());
            pt.setProject(p);
            pt.setTask(task);
            em.persist(pt);
            log.log(Level.INFO, "#### Initial ProjectTask added: {0}", task.getTaskName());
        }
    }

    private void addinitialProjectStatusType(Company c) {
        ProjectStatusType p1 = new ProjectStatusType();
        p1.setCompany(c);
        p1.setProjectStatusName("Project is ahead of schedule");
        p1.setStatusColor((short) TaskStatusTypeDTO.STATUS_COLOR_GREEN);
        em.persist(p1);
        ProjectStatusType p2 = new ProjectStatusType();
        p2.setCompany(c);
        p2.setStatusColor((short) TaskStatusTypeDTO.STATUS_COLOR_GREEN);
        p2.setProjectStatusName("Project is on schedule");
        em.persist(p2);
        ProjectStatusType p3 = new ProjectStatusType();
        p3.setCompany(c);
        p3.setStatusColor((short) TaskStatusTypeDTO.STATUS_COLOR_GREEN);
        p3.setProjectStatusName("Project is complete");
        em.persist(p3);
        ProjectStatusType p4 = new ProjectStatusType();
        p4.setCompany(c);
        p4.setProjectStatusName("Project is behind schedule");
        p4.setStatusColor((short) TaskStatusTypeDTO.STATUS_COLOR_RED);
        em.persist(p4);
        ProjectStatusType p5 = new ProjectStatusType();
        p5.setStatusColor((short) TaskStatusTypeDTO.STATUS_COLOR_GREEN);
        p5.setCompany(c);
        p5.setProjectStatusName("Project is on budget");
        em.persist(p5);
        ProjectStatusType p6 = new ProjectStatusType();
        p6.setCompany(c);
        p6.setStatusColor((short) TaskStatusTypeDTO.STATUS_COLOR_AMBER);
        p6.setProjectStatusName("Project is over budget");
        em.persist(p6);

        log.log(Level.INFO, "*** Initial ProjectStatusTypes added");
    }

    private void addInitialTaskStatusTypes(Company c) {
        TaskStatusType ts1 = new TaskStatusType();
        ts1.setTaskStatusTypeName("Completed");
        ts1.setCompany(c);
        ts1.setStatusColor((short) TaskStatusTypeDTO.STATUS_COLOR_GREEN);
        em.persist(ts1);
        TaskStatusType ts2 = new TaskStatusType();
        ts2.setTaskStatusTypeName("Delayed - Weather");
        ts2.setStatusColor((short) TaskStatusTypeDTO.STATUS_COLOR_RED);
        ts2.setCompany(c);
        em.persist(ts2);
        TaskStatusType ts3 = new TaskStatusType();
        ts3.setTaskStatusTypeName("Delayed - Staff");
        ts3.setStatusColor((short) TaskStatusTypeDTO.STATUS_COLOR_RED);
        ts3.setCompany(c);
        em.persist(ts3);
        TaskStatusType ts4 = new TaskStatusType();
        ts4.setTaskStatusTypeName("Delayed - Materials");
        ts4.setStatusColor((short) TaskStatusTypeDTO.STATUS_COLOR_RED);
        ts4.setCompany(c);
        em.persist(ts4);
        TaskStatusType ts5 = new TaskStatusType();
        ts5.setTaskStatusTypeName("Not started yet");
        ts5.setStatusColor((short) TaskStatusTypeDTO.STATUS_COLOR_AMBER);
        ts5.setCompany(c);
        em.persist(ts5);
        TaskStatusType ts6 = new TaskStatusType();
        ts6.setTaskStatusTypeName("Everything Normal");
        ts6.setCompany(c);
        ts6.setStatusColor((short) TaskStatusTypeDTO.STATUS_COLOR_GREEN);
        em.persist(ts6);

        log.log(Level.INFO, "Initial TaskStatusTypes added");
    }

    public String getErrorString(Exception e) {

        StringBuilder sb = new StringBuilder();
        try {
            if (e.getMessage() != null) {
                sb.append(e.getMessage()).append("\n\n");
            }
            if (e.toString() != null) {
                sb.append(e.toString()).append("\n\n");
            }
            StackTraceElement[] s = e.getStackTrace();
            if (s.length > 0) {
                StackTraceElement ss = s[0];
                String method = ss.getMethodName();
                String cls = ss.getClassName();
                int line = ss.getLineNumber();
                sb.append("Class: ").append(cls).append("\n");
                sb.append("Method: ").append(method).append("\n");
                sb.append("Line Number: ").append(line).append("\n");
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed, ignored " + ex.getMessage());
        }

        return sb.toString();
    }

    public String getRandomPin() {
        StringBuilder sb = new StringBuilder();
        Random rand = new Random(System.currentTimeMillis());
        int x = rand.nextInt(9);
        if (x == 0) {
            x = 3;
        }
        sb.append(x);
        sb.append(rand.nextInt(9));
        sb.append(rand.nextInt(9));
        sb.append(rand.nextInt(9));
        sb.append(rand.nextInt(9));
        sb.append(rand.nextInt(9));
        return sb.toString();
    }
    final Logger log = Logger.getLogger(DataUtil.class
            .getSimpleName());

    public ResponseDTO updateMonitor(MonitorDTO mon) throws DataException {
        ResponseDTO w = new ResponseDTO();

        try {
            Monitor monitor = em.find(Monitor.class, mon.getMonitorID());
            if (mon.getFirstName() != null) {
                monitor.setFirstName(mon.getFirstName());
            }
            if (mon.getCellphone() != null) {
                monitor.setCellphone(mon.getCellphone());
            }
            if (mon.getEmail() != null) {
                monitor.setEmail(mon.getEmail());
            }
            if (mon.getLastName() != null) {
                monitor.setLastName(mon.getLastName());
            }
            if (mon.getActiveFlag() != null) {
                monitor.setActiveFlag(mon.getActiveFlag());
            }
            if (mon.getIDNumber() != null) {
                monitor.setIDNumber(mon.getIDNumber());
            }
            if (mon.getAddress() != null) {
                monitor.setAddress(mon.getAddress());
            }
            if (mon.getGender() != null) {
                monitor.setGender(mon.getGender());
            }

            em.merge(monitor);
            w.setMonitorList(new ArrayList<>());
            w.getMonitorList().add(new MonitorDTO(monitor));
            w.setMessage("Monitor data updated");
            log.log(Level.OFF, "Monitor data updated: {0} {1}", new Object[]{monitor.getFirstName(), monitor.getLastName()});
        } catch (Exception e) {
            throw new DataException("Failed to update monitor");
        }
        return w;
    }

    public void updateProjectStatusType(ProjectStatusTypeDTO dto) throws DataException {
        try {
            ProjectStatusType ps = em.find(ProjectStatusType.class, dto.getProjectStatusTypeID());
            if (ps != null) {
                if (dto.getProjectStatusName() != null) {
                    ps.setProjectStatusName(dto.getProjectStatusName());
                }
                if (dto.getStatusColor() != null) {
                    ps.setStatusColor(dto.getStatusColor());
                }
                em.merge(ps);
                log.log(Level.INFO, "Project Status Type updated");
            }
        } catch (Exception e) {
            log.log(Level.OFF, null, e);
            throw new DataException("Failed to update project status type\n" + getErrorString(e));
        }

    }

    public void updateTaskStatus(TaskStatusTypeDTO dto) throws DataException {
        try {
            TaskStatusType ps = em.find(TaskStatusType.class, dto.getTaskStatusTypeID());
            if (ps != null) {
                if (dto.getTaskStatusTypeName() != null) {
                    ps.setTaskStatusTypeName(dto.getTaskStatusTypeName());
                }
                if (dto.getStatusColor() != null) {
                    ps.setStatusColor(dto.getStatusColor());
                }
                em.merge(ps);
                log.log(Level.INFO, "Task Status Type updated");
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            log.log(Level.OFF, null, e);
            throw new DataException("Failed to update statusType\n" + getErrorString(e));
        }

    }

    public void updateTask(TaskDTO dto) throws DataException {
        try {
            Task ps = em.find(Task.class, dto.getTaskID());
            if (ps != null) {
                if (dto.getTaskName() != null) {
                    ps.setTaskName(dto.getTaskName());
                }
                if (dto.getDescription() != null) {
                    ps.setDescription(dto.getDescription());
                }
                if (dto.getTaskNumber() != null) {
                    ps.setTaskNumber(dto.getTaskNumber());
                }
                em.merge(ps);

                log.log(Level.INFO, "Task updated");
            }
        } catch (Exception e) {
            log.log(Level.OFF, null, e);
            throw new DataException("Failed to update task\n" + getErrorString(e));
        }

    }

    public ResponseDTO setNewPin(Integer staffID) throws DataException {
        ResponseDTO resp = new ResponseDTO();

        try {
            Staff cs = em.find(Staff.class, staffID);
            cs.setPin(getRandomPin());
            em.merge(cs);
            em.flush();
            resp.setStaffList(new ArrayList<>());
            resp.getStaffList().add(new StaffDTO(cs));

        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException(("Failed to set PIN\n" + getErrorString(e)));
        }
        return resp;
    }

    public void updateDevice(GcmDeviceDTO dto) throws DataException {
        try {

            GcmDevice device = em.find(GcmDevice.class, dto.getGcmDeviceID());
            device.setRegistrationID(dto.getRegistrationID());
            device.setAndroidVersion(dto.getAndroidVersion());

            em.merge(device);
            log.log(Level.INFO, "Device  updated: {0} Android: {1}", new Object[]{device.getModel(), device.getAndroidVersion()});

        } catch (Exception e) {
            log.log(Level.OFF, null, e);
            throw new DataException("Failed to update project\n" + getErrorString(e));
        }

    }

    public void updateProject(ProjectDTO dto) throws DataException {
        try {
            Project ps = em.find(Project.class, dto.getProjectID());
            if (ps != null) {
                if (dto.getProjectName() != null) {
                    ps.setProjectName(dto.getProjectName());
                }
                if (dto.getDescription() != null) {
                    ps.setDescription(dto.getDescription());
                }
                if (dto.getLatitude() != null) {
                    ps.setLatitude(dto.getLatitude());
                }
                if (dto.getLongitude() != null) {
                    ps.setLongitude(dto.getLongitude());
                }
                em.merge(ps);
                log.log(Level.INFO, "Project updated");
            }
        } catch (Exception e) {
            log.log(Level.OFF, null, e);
            throw new DataException("Failed to update project\n" + getErrorString(e));
        }

    }

    public void updateStaffProjects(List<StaffProjectDTO> dtoList) throws DataException {
        try {
            for (StaffProjectDTO dto : dtoList) {
                StaffProject ps = em.find(StaffProject.class, dto.getStaffProjectID());
                ps.setActiveFlag(dto.getActiveFlag());
                em.merge(ps);
                log.log(Level.INFO, "StaffProject  updated, activeFlag: {0}", dto.getActiveFlag());
            }

        } catch (Exception e) {
            log.log(Level.OFF, null, e);
            throw new DataException("Failed to update staffProject\n" + getErrorString(e));
        }
    }

    public void confirmLocation(Integer projectID, double latitude, double longitude, Float accuracy) throws DataException {
        try {
            Project ps = em.find(Project.class, projectID);
            if (ps != null) {
                ps.setLocationConfirmed(Boolean.TRUE);
                ps.setLatitude(latitude);
                ps.setLongitude(longitude);
                ps.setAccuracy(accuracy);
                em.merge(ps);
                log.log(Level.INFO, "Project Site location confirmed");
            }
        } catch (Exception e) {
            log.log(Level.OFF, null, e);
            throw new DataException("Failed to confirm location\n" + getErrorString(e));
        }
    }


    public void fixData() {

        Company c = em.find(Company.class, 30);
        Query q = em.createNamedQuery("TaskType.findAll", TaskType.class
        );
        List<TaskType> ttList = q.getResultList();
        int number = 1;
        List<Task> taskList = new ArrayList<>(ttList.size());
        for (TaskType tt : ttList) {
            Task task = new Task();
            task.setCompany(c);
            task.setTaskName(tt.getTaskTypeName());
            task.setTaskNumber(number);
            try {
                em.persist(task);
                em.flush();
                taskList.add(task);
                number++;
                System.out.println("Task: " + task.getTaskName());
            } catch (Exception e) {
                number++;
            }

        }

    }

    public void fixProjectTasks() {
        Query q = em.createNamedQuery("Task.findByCompany", Task.class
        );
        q.setParameter("companyID", 30);
        List<Task> taskList = q.getResultList();

        writeProjectTasks(63, taskList);
        writeProjectTasks(67, taskList);

    }

    private void writeProjectTasks(Integer programmeID, List<Task> taskList) {
        Query q = em.createNamedQuery("Project.findByProgramme", Project.class
        );
        q.setParameter("programmeID", programmeID);
        List<Project> pList = q.getResultList();
        for (Project proj : pList) {
            for (Task task : taskList) {
                ProjectTask pt = new ProjectTask();
                pt.setProject(proj);
                pt.setTask(task);
                pt.setDateRegistered(new Date());
                try {
                    em.persist(pt);
                    System.out.println("Project: " + proj.getProjectName() + "\tTask: " + task.getTaskName());
                } catch (Exception e) {

                }
            }
            System.out.println("\n#########################\n");
        }
    }

    public void writeStaffProjects() {
        Company c = em.find(Company.class, 30);
        Query q = em.createNamedQuery("Staff.findByCompany", Staff.class
        );
        q.setParameter("companyID", 32);
        List<Staff> pList = q.getResultList();
        List<Staff> staffList = new ArrayList<>(pList.size());
        q
                = em.createNamedQuery("Project.findByProgramme", Project.class
                );
        q.setParameter("programmeID", 67);
        List<Project> projList = q.getResultList();
        for (Staff s : pList) {
            Staff st = new Staff();
            st.setActiveFlag(1);
            st.setFirstName(s.getFirstName());
            st.setLastName(s.getLastName());
            st.setCellphone(s.getCellphone());
            st.setCompany(c);
            st.setEmail(s.getEmail());
            st.setPin(s.getPin());
            st.setDateRegistered(new Date());
            em.persist(st);
            em.flush();
            staffList.add(st);
        }
        System.out.println("\n######### STAFF ###################\n");
        for (Staff s : staffList) {
            for (Project p : projList) {
                StaffProject sp = new StaffProject();
                sp.setProject(p);
                sp.setStaff(s);
                sp.setDateAssigned(new Date());
                sp.setActiveFlag(Boolean.TRUE);
                em.persist(sp);
                System.out.println("Staff: " + s.getFirstName() + " " + s.getLastName()
                        + " Project: " + p.getProjectName());

            }

        }

    }

    public void writeMonitorProjects() {
        Company c = em.find(Company.class, 30);
        Query q = em.createNamedQuery("Monitor.findByCompany", Monitor.class
        );
        q.setParameter("companyID", 32);
        List<Monitor> pList = q.getResultList();
        List<Monitor> monList = new ArrayList<>(pList.size());
        q
                = em.createNamedQuery("Project.findByProgramme", Project.class
                );
        q.setParameter("programmeID", 67);
        List<Project> projList = q.getResultList();
        for (Monitor s : pList) {
            Monitor st = new Monitor();
            st.setActiveFlag(1);
            st.setFirstName(s.getFirstName());
            st.setLastName(s.getLastName());
            st.setCellphone(s.getCellphone());
            st.setCompany(c);
            st.setEmail(s.getEmail());
            st.setPin(s.getPin());
            st.setDateRegistered(new Date());
            em.persist(st);
            em.flush();
            monList.add(st);
        }
        System.out.println("\n########## MONITORS ##################\n");
        for (Monitor s : monList) {
            for (Project p : projList) {
                MonitorProject sp = new MonitorProject();
                sp.setProject(p);
                sp.setMonitor(s);
                sp.setDateAssigned(new Date());
                sp.setActiveFlag(Boolean.TRUE);
                em.persist(sp);
                System.out.println("Monitor: " + s.getFirstName() + " " + s.getLastName()
                        + " Project: " + p.getProjectName());

            }

        }

    }
}
