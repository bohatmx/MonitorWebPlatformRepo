/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boha.monitor.utilx;

import com.boha.monitor.data.*;
import com.boha.monitor.dto.*;
import com.boha.monitor.dto.transfer.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.joda.time.DateTime;

/**
 *
 * @author aubreyM
 */
public class ListUtil {

    public static ResponseDTO getLookups(EntityManager em, Integer companyID) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        resp.setPhotoUploadList(new ArrayList<>());
        try {
            Company c = em.find(Company.class, companyID);
            //find profile photos
            Query a = em.createNamedQuery("PhotoUpload.findCompanyPhotosByPictureType", PhotoUpload.class);
            a.setParameter("companyID", companyID);
            List<Integer> types = new ArrayList<>(2);
            types.add(PhotoUploadDTO.STAFF_IMAGE);
            types.add(PhotoUploadDTO.MONITOR_IMAGE);

            a.setParameter("pictureTypes", types);
            List<PhotoUpload> phList = a.getResultList();

            Query q = em.createNamedQuery("Staff.findByCompany", Staff.class);
            q.setParameter("companyID", companyID);
            List<Staff> staffList = q.getResultList();
            for (Staff staff : staffList) {
                if (staff.getActiveFlag() == 1) {
                    StaffDTO s = new StaffDTO(staff);
                    for (PhotoUpload x : phList) {
                        Staff staffFromPhoto = x.getStaff();
                        if (staffFromPhoto != null) {
                            if (Objects.equals(staff.getStaffID(), staffFromPhoto.getStaffID())) {
                                s.getPhotoUploadList().add(new PhotoUploadDTO(x));
                            }
                        }
                    }
                    resp.getStaffList().add(s);
                }
            }
            q = em.createNamedQuery("Monitor.findByCompany", Monitor.class);
            q.setParameter("companyID", companyID);
            List<Monitor> monList = q.getResultList();
            for (Monitor mon : monList) {
                if (mon.getActiveFlag() == 1) {
                    MonitorDTO s = new MonitorDTO(mon);
                    for (PhotoUpload x : phList) {
                        if (Objects.equals(s.getMonitorID(), x.getMonitor().getMonitorID())) {
                            s.getPhotoUploadList().add(new PhotoUploadDTO(x));
                        }
                    }
                    resp.getMonitorList().add(s);
                }
            }
            q = em.createNamedQuery("Task.findByCompany", Monitor.class);
            q.setParameter("companyID", companyID);
            List<Task> taskList = q.getResultList();
            for (Task task : taskList) {
                resp.getTaskList().add(new TaskDTO(task));
            }
            q = em.createNamedQuery("TaskStatusType.findByCompany", TaskStatusType.class);
            q.setParameter("companyID", companyID);
            List<TaskStatusType> tstList = q.getResultList();
            for (TaskStatusType type : tstList) {
                resp.getTaskStatusTypeList().add(new TaskStatusTypeDTO(type));
            }
            log.log(Level.OFF, "Company lookup data: tasks: {0} statusTypes: {1} staff: {2} monitors: {3}",
                    new Object[]{resp.getTaskList().size(), resp.getTaskStatusTypeList().size(),
                        resp.getStaffList().size(), resp.getMonitorList().size()});
        } catch (Exception e) {
            throw new DataException(e.getMessage());
        }
        return resp;
    }

    public static ResponseDTO getMonitorPhotos(EntityManager em, Integer monitorID) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        resp.setPhotoUploadList(new ArrayList<>());
        try {
            Query q = em.createNamedQuery("PhotoUpload.findByMonitor", PhotoUpload.class);
            q.setParameter("monitorID", monitorID);
            List<PhotoUpload> list = q.getResultList();
            for (PhotoUpload photoUpload : list) {
                resp.getPhotoUploadList().add(new PhotoUploadDTO(photoUpload));
            }

        } catch (Exception e) {

            throw new DataException(null);
        }
        return resp;
    }

    public static ResponseDTO getStaffPhotos(EntityManager em, Integer staffID) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        resp.setPhotoUploadList(new ArrayList<>());
        try {
            Query q = em.createNamedQuery("PhotoUpload.findByStaff", PhotoUpload.class);
            q.setParameter("staffID", staffID);
            List<PhotoUpload> list = q.getResultList();
            for (PhotoUpload photoUpload : list) {
                resp.getPhotoUploadList().add(new PhotoUploadDTO(photoUpload));
            }

        } catch (Exception e) {

            throw new DataException(null);
        }
        return resp;
    }

    public static ResponseDTO getProjectsForMonitorAssignment(EntityManager em,
            Integer companyID, Integer monitorID) throws DataException {
        ResponseDTO resp = new ResponseDTO();

        try {
            Query q = em.createNamedQuery("Project.findByCompany", Project.class);
            q.setParameter("companyID", companyID);
            List<Project> list = q.getResultList();
            resp.setProjectList(new ArrayList<>());
            for (Project p : list) {
                ProjectDTO d = new ProjectDTO();
                d.setProjectID(p.getProjectID());
                d.setProjectName(p.getProjectName());
                if (p.getCity() != null) {
                    d.setCityName(p.getCity().getCityName());
                    if (p.getCity().getMunicipality() != null) {
                        d.setMunicipalityName(p.getCity().getMunicipality().getMunicipalityName());
                    }
                    d.setLatitude(p.getLatitude());
                    d.setLongitude(p.getLongitude());
                    resp.getProjectList().add(d);
                }
            }
            q = em.createNamedQuery("MonitorProject.findMonitorProjects", MonitorProject.class);
            q.setParameter("monitorID", monitorID);
            List<MonitorProject> mpList = q.getResultList();
            resp.setMonitorProjectList(new ArrayList<>());
            for (MonitorProject mp : mpList) {
                resp.getMonitorProjectList().add(new MonitorProjectDTO(mp));
            }
        } catch (Exception e) {
            log.log(Level.OFF, "Failed", e);
            throw new DataException("Falied to get monitor projects");
        }

        return resp;
    }

    public static ResponseDTO getProjectsForStaffAssignment(EntityManager em,
            Integer companyID, Integer staffID) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        try {
            Query q = em.createNamedQuery("Project.findByCompany", Project.class);
            q.setParameter("companyID", companyID);
            List<Project> list = q.getResultList();
            resp.setProjectList(new ArrayList<>());
            for (Project p : list) {
                ProjectDTO d = new ProjectDTO();
                d.setProjectID(p.getProjectID());
                d.setProjectName(p.getProjectName());
                if (p.getCity() != null) {
                    d.setCityName(p.getCity().getCityName());
                    if (p.getCity().getMunicipality() != null) {
                        d.setMunicipalityName(p.getCity().getMunicipality().getMunicipalityName());
                    }
                    d.setLatitude(p.getLatitude());
                    d.setLongitude(p.getLongitude());
                    resp.getProjectList().add(d);
                }
            }
            q = em.createNamedQuery("StaffProject.findStaffProjects", StaffProject.class);
            q.setParameter("staffID", staffID);
            List<StaffProject> mpList = q.getResultList();
            resp.setStaffProjectList(new ArrayList<>());
            for (StaffProject mp : mpList) {
                resp.getStaffProjectList().add(new StaffProjectDTO(mp));
            }
        } catch (Exception e) {
            log.log(Level.OFF, "Failed", e);
            throw new DataException("Falied to get staff projects");
        }

        return resp;
    }

    public static ResponseDTO getMonitorSummary(EntityManager em, Integer monitorID) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        Monitor monitor = em.find(Monitor.class, monitorID);
        Query q = em.createNamedQuery("MonitorProject.findProjectsByMonitor", Project.class);
        q.setParameter("monitorID", monitorID);
        List<Project> projectList = q.getResultList();
        resp.setProjectList(new ArrayList<>());

        MonitorDTO dto = new MonitorDTO(monitor);
        dto.setPhotoUploadList(new ArrayList<>());
        q = em.createNamedQuery("PhotoUpload.findByMonitor", PhotoUpload.class);
        q.setParameter("monitorID", dto.getMonitorID());
        List<PhotoUpload> pList = q.getResultList();

        dto.setPhotoCount(pList.size());
        dto.setProjectCount(projectList.size());

        q = em.createNamedQuery("ProjectTaskStatus.findByMonitor", ProjectTaskStatus.class);
        q.setParameter("monitorID", monitor.getMonitorID());
        List<ProjectTaskStatus> pts = q.getResultList();
        if (pts.size() > 0) {
            dto.setLastStatus(new ProjectTaskStatusDTO(pts.get(0)));
        }
        dto.setStatusCount(pts.size());

        q = em.createNamedQuery("LocationTracker.findByMonitor", LocationTracker.class);
        q.setParameter("monitorID", monitor.getMonitorID());
        q.setMaxResults(3);
        List<LocationTracker> ltList = q.getResultList();
        dto.setLocationTrackerList(new ArrayList<>());
        for (LocationTracker t : ltList) {
            dto.getLocationTrackerList().add(new LocationTrackerDTO(t));
        }

        for (Project p : projectList) {
            ProjectDTO project = new ProjectDTO(p);
            resp.getProjectList().add(project);
        }
        resp.setMonitorList(new ArrayList<>());
        resp.getMonitorList().add(dto);
        return resp;
    }

    /**
     * Get all the data needed for the Monitor app. This includes data about the
     * projects the monitor is assigned to. Also lists all the monitors assigned
     * to the same projects as the requesting monitor.
     *
     * @param em
     * @param monitorID
     * @return
     * @throws com.boha.monitor.utilx.DataException
     */
    public static ResponseDTO getProjectsForMonitor(EntityManager em, Integer monitorID) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        Query q = em.createNamedQuery("ProjectTask.findByMonitor", ProjectTask.class);
        q.setParameter("monitorID", monitorID);
        q.setParameter("activeFlag", Boolean.TRUE);
        List<ProjectTask> ptList = q.getResultList();

        q = em.createNamedQuery("PhotoUpload.findByMonitorProject", PhotoUpload.class);
        q.setParameter("monitorID", monitorID);
        List<PhotoUpload> phList = q.getResultList();

        q = em.createNamedQuery("ProjectTaskStatus.countByMonitorProjectTask");
        q.setParameter("monitorID", monitorID);
        List<Object> objList = q.getResultList();
        
        q = em.createNamedQuery("VideoUpload.findByMonitorProject", VideoUpload.class);
        q.setParameter("monitorID", monitorID);
        List<VideoUpload> vList = q.getResultList();

        q = em.createNamedQuery("MonitorProject.findMonitorProjects", MonitorProject.class);
        q.setParameter("monitorID", monitorID);
        List<MonitorProject> sList = q.getResultList();

        List<Integer> ids = new ArrayList<>();
        for (MonitorProject sp : sList) {
            ids.add(sp.getProject().getProjectID());
        }
        q = em.createNamedQuery("MonitorProject.countMonitorsByProject");
        q.setParameter("list", ids);
        List<Object> monCountList = q.getResultList();

        q = em.createNamedQuery("MonitorProject.countMonitorsByProject");
        q.setParameter("list", ids);
        List<Object> monitorCountList = q.getResultList();

        for (MonitorProject p : sList) {
            if (Objects.equals(p.getActiveFlag(), Boolean.TRUE)) {
                ProjectDTO project = new ProjectDTO(p.getProject());
                for (ProjectTask pt : ptList) {
                    if (Objects.equals(pt.getProject().getProjectID(), project.getProjectID())) {
                        ProjectTaskDTO dto = new ProjectTaskDTO(pt);
                        for (Object obj : objList) {
                            Object[] arr = (Object[]) obj;
                            Integer id = (Integer) arr[0];
                            Long cnt = (Long) arr[1];
                            if (Objects.equals(id, dto.getProjectTaskID())) {
                                dto.setStatusCount(cnt.intValue());
                                project.setStatusCount(project.getStatusCount() + cnt.intValue());
                            }
                        }

                        project.getProjectTaskList().add(dto);
                    }
                }
                project.setProjectTaskCount(project.getProjectTaskList().size());
                for (PhotoUpload pt : phList) {
                    if (Objects.equals(pt.getProject().getProjectID(), project.getProjectID())) {
                        project.getPhotoUploadList().add(new PhotoUploadDTO(pt));
                    }
                }
                project.setPhotoCount(project.getPhotoUploadList().size());
                
                for (VideoUpload pt : vList) {
                    if (Objects.equals(pt.getProject().getProjectID(), project.getProjectID())) {
                        project.getVideoUploadList().add(new VideoUploadDTO(pt));
                    }
                }
                project.setVideoCount(project.getVideoUploadList().size());
                
                for (Object ob : monCountList) {
                    Object[] arr = (Object[]) ob;
                    Integer id = (Integer) arr[0];
                    Long cnt = (Long) arr[1];
                    if (Objects.equals(id, project.getProjectID())) {
                        project.setStaffCount(cnt.intValue());
                    }
                }
                for (Object ob : monitorCountList) {
                    Object[] arr = (Object[]) ob;
                    Integer id = (Integer) arr[0];
                    Long cnt = (Long) arr[1];
                    if (Objects.equals(id, project.getProjectID())) {
                        project.setMonitorCount(cnt.intValue());
                    }
                }
//                log.log(Level.OFF, "project photos: {0} statusCount: {1} staffCount: {2} monitorCount {3} - {4}", 
//                        new Object[]{project.getPhotoCount(), project.getStatusCount(), project.getStaffCount(), 
//                            project.getMonitorCount(),project.getProjectName()});
                resp.getProjectList().add(project);
            }
        }
        Collections.sort(resp.getProjectList());
        log.log(Level.OFF, "Monitor Projects found: {0}",
                new Object[]{resp.getProjectList().size()});

        return resp;
    }

    public static ResponseDTO getProjectsForStaff(EntityManager em, Integer staffID) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        Query q = em.createNamedQuery("ProjectTask.findByStaff", ProjectTask.class);
        q.setParameter("staffID", staffID);
        q.setParameter("activeFlag", Boolean.TRUE);
        List<ProjectTask> ptList = q.getResultList();

        q = em.createNamedQuery("PhotoUpload.findByStaffProject", PhotoUpload.class);
        q.setParameter("staffID", staffID);
        List<PhotoUpload> phList = q.getResultList();
        
        q = em.createNamedQuery("VideoUpload.findByStaffProject", VideoUpload.class);
        q.setParameter("staffID", staffID);
        List<VideoUpload> vList = q.getResultList();
        log.log(Level.INFO, "Project Videos found: {0}", vList.size());

        q = em.createNamedQuery("ProjectTaskStatus.countByStaffProjectTask");
        q.setParameter("staffID", staffID);
        List<Object> objList = q.getResultList();

        q = em.createNamedQuery("StaffProject.findByStaff", StaffProject.class);
        q.setParameter("staffID", staffID);
        List<StaffProject> sList = q.getResultList();

        List<Integer> ids = new ArrayList<>();
        for (StaffProject sp : sList) {
            ids.add(sp.getProject().getProjectID());
        }
        q = em.createNamedQuery("StaffProject.countStaffByProject");
        q.setParameter("list", ids);
        List<Object> staffCountList = q.getResultList();

        q = em.createNamedQuery("MonitorProject.countMonitorsByProject");
        q.setParameter("list", ids);
        List<Object> monitorCountList = q.getResultList();

        for (StaffProject p : sList) {
            if (Objects.equals(p.getActiveFlag(), Boolean.TRUE)) {
                ProjectDTO project = new ProjectDTO(p.getProject());
                for (ProjectTask pt : ptList) {
                    if (Objects.equals(pt.getProject().getProjectID(), project.getProjectID())) {
                        ProjectTaskDTO dto = new ProjectTaskDTO(pt);
                        for (Object obj : objList) {
                            Object[] arr = (Object[]) obj;
                            Integer id = (Integer) arr[0];
                            Long cnt = (Long) arr[1];
                            if (Objects.equals(id, dto.getProjectTaskID())) {
                                dto.setStatusCount(cnt.intValue());
                                project.setStatusCount(project.getStatusCount() + cnt.intValue());
                            }
                        }

                        project.getProjectTaskList().add(dto);
                    }
                }
                project.setProjectTaskCount(project.getProjectTaskList().size());
                for (PhotoUpload pt : phList) {
                    if (Objects.equals(pt.getProject().getProjectID(), project.getProjectID())) {
                        project.getPhotoUploadList().add(new PhotoUploadDTO(pt));
                    }
                }
                project.setPhotoCount(project.getPhotoUploadList().size());
                
                for (VideoUpload pt : vList) {
                    if (Objects.equals(pt.getProject().getProjectID(), project.getProjectID())) {
                        project.getVideoUploadList().add(new VideoUploadDTO(pt));
                    }
                }
                project.setVideoCount(project.getVideoUploadList().size());
                
                for (Object ob : staffCountList) {
                    Object[] arr = (Object[]) ob;
                    Integer id = (Integer) arr[0];
                    Long cnt = (Long) arr[1];
                    if (Objects.equals(id, project.getProjectID())) {
                        project.setStaffCount(cnt.intValue());
                    }
                }
                for (Object ob : monitorCountList) {
                    Object[] arr = (Object[]) ob;
                    Integer id = (Integer) arr[0];
                    Long cnt = (Long) arr[1];
                    if (Objects.equals(id, project.getProjectID())) {
                        project.setMonitorCount(cnt.intValue());
                    }
                }
//                log.log(Level.OFF, "project photos: {0} statusCount: {1} staffCount: {2} monitorCount {3} - {4}", 
//                        new Object[]{project.getPhotoCount(), project.getStatusCount(), project.getStaffCount(), 
//                            project.getMonitorCount(),project.getProjectName()});
                resp.getProjectList().add(project);
            }
        }
        Collections.sort(resp.getProjectList());
        log.log(Level.OFF, "Staff Projects found: {0}",
                new Object[]{resp.getProjectList().size()});

        return resp;
    }

    public static ResponseDTO getLatestDeviceLocations(EntityManager em, Integer companyID) throws DataException {
        log.log(Level.INFO, "########### getLatestDeviceLocations, companyID: {0}", companyID);
        ResponseDTO r = new ResponseDTO();
        try {

            Query qq = em.createNamedQuery("Staff.findByCompany", Staff.class);
            qq.setParameter("companyID", companyID);
            List<Staff> staffList = qq.getResultList();
            qq = em.createNamedQuery("LocationTracker.findByStaff", LocationTracker.class);
            for (Staff staff : staffList) {
                qq.setParameter("staffID", staff.getStaffID());
                qq.setMaxResults(1);
                List<LocationTracker> locList = qq.getResultList();
                if (!locList.isEmpty()) {
                    LocationTrackerDTO ltd = new LocationTrackerDTO(locList.get(0));
                    r.getLocationTrackerList().add(ltd);
                }
            }
            qq = em.createNamedQuery("Monitor.findByCompany", Monitor.class);
            qq.setParameter("companyID", companyID);
            List<Monitor> monList = qq.getResultList();
            qq = em.createNamedQuery("LocationTracker.findByMonitor", LocationTracker.class);
            for (Monitor mon : monList) {
                qq.setParameter("monitorID", mon.getMonitorID());
                qq.setMaxResults(1);
                List<LocationTracker> locList = qq.getResultList();
                if (!locList.isEmpty()) {
                    LocationTrackerDTO ltd = new LocationTrackerDTO(locList.get(0));
                    r.getLocationTrackerList().add(ltd);
                }
            }

            log.log(Level.INFO, "Latest device locations: {0} CompanyID: {1}", new Object[]{r.getLocationTrackerList().size(), companyID});

            StringBuilder sb = new StringBuilder();
            sb.append("Latest locationTracks").append("\n");
            r.getLocationTrackerList().stream().map((t) -> {
                sb.append("id: ").append(t.getLocationTrackerID()).append(" ");
                return t;
            }).forEach((t) -> {
                sb.append(new Date(t.getDateTracked()).toString()).append("\n");
            });
            log.log(Level.INFO, sb.toString());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Fail", e);
            throw new DataException("");
        }
        return r;
    }

    public static ResponseDTO getCompanyTasks(EntityManager em, Integer companyID) throws DataException {
        ResponseDTO r = new ResponseDTO();
        Query q = em.createNamedQuery("Task.findByCompany", Task.class);
        q.setParameter("companyID", companyID);
        List<Task> list = q.getResultList();
        list.stream().forEach((task) -> {
            r.getTaskList().add(new TaskDTO(task));
        });
        return r;
    }

    public static ResponseDTO getCompanyData(EntityManager em, Integer companyID) throws DataException {
        ResponseDTO resp = new ResponseDTO();

        resp.setStaffList(getCompanyStaffList(em, companyID).getStaffList());
        resp.setTaskStatusTypeList(getTaskStatusTypeList(em, companyID).getTaskStatusTypeList());
        resp.setProjectStatusTypeList(getProjectStatusTypeList(em, companyID).getProjectStatusTypeList());
        resp.setMonitorList(getMonitorList(em, companyID).getMonitorList());
        resp.setPortfolioList(getPortfolioList(em, companyID).getPortfolioList());

        return resp;
    }

    public static ResponseDTO getPortfolioList(EntityManager em, Integer companyID) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        Query q = em.createNamedQuery("Portfolio.findByCompany", Portfolio.class);
        q.setParameter("companyID", companyID);
        List<Portfolio> tList = q.getResultList();
        resp.setPortfolioList(new ArrayList<>());

        for (Portfolio p : tList) {
            PortfolioDTO dto = new PortfolioDTO(p);
            dto.setProgrammeList(getProgrammeList(em, p.getPortfolioID()));
            resp.getPortfolioList().add(dto);

        }

        resp.setMonitorList(getMonitorList(em, companyID).getMonitorList());
        resp.setStaffList(getCompanyStaffList(em, companyID).getStaffList());
        return resp;
    }

    public static ResponseDTO getCompanyList(EntityManager em) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        Query q = em.createNamedQuery("Company.findAll", Company.class);
        List<Company> tList = q.getResultList();
        resp.setCompanyList(new ArrayList<>());
        for (Company p : tList) {
            CompanyDTO company = new CompanyDTO(p);
            ResponseDTO r = getCompanyData(em, p.getCompanyID());
            company.setStaffList(r.getStaffList());
            company.setMonitorList(r.getMonitorList());
            company.setPortfolioList(r.getPortfolioList());
            company.setProjectStatusTypeList(r.getProjectStatusTypeList());
            company.setTaskStatusTypeList(r.getTaskStatusTypeList());

            resp.getCompanyList().add(company);
        }
        return resp;
    }

    public static List<ProgrammeDTO> getProgrammeList(EntityManager em, Integer portfolioID) throws DataException {
        List<ProgrammeDTO> list = new ArrayList<>();

        Query q = em.createNamedQuery("Programme.findByPortfolio", Programme.class);
        q.setParameter("portfolioID", portfolioID);
        List<Programme> tList = q.getResultList();
        for (Programme programme : tList) {
            ProgrammeDTO dto = new ProgrammeDTO(programme);
            dto.setProjectList(getProjectList(em, programme.getProgrammeID()));
            // dto.setTaskTypeList(getProgrammeTaskTypeList(em,
            // dto.getProgrammeID()).getTaskTypeList());
            list.add(dto);
        }

        return list;
    }

    public static ResponseDTO getLocationTracksByDevice(EntityManager em, Integer deviceID) {
        ResponseDTO resp = new ResponseDTO();
        resp.setLocationTrackerList(new ArrayList<>());
        Query q = em.createNamedQuery("LocationTracker.findByDevice", LocationTracker.class);
        q.setParameter("gcmDeviceID", deviceID);
        List<LocationTracker> tList = q.getResultList();
        for (LocationTracker gcm : tList) {
            resp.getLocationTrackerList().add(new LocationTrackerDTO(gcm));
        }
        log.log(Level.OFF, "Device locations found: {0}", resp.getLocationTrackerList().size());
        return resp;
    }

    public static ResponseDTO getDeviceList(EntityManager em, Integer companyID) {
        ResponseDTO resp = new ResponseDTO();
        resp.setGcmDeviceList(new ArrayList<>());
        Query q = em.createNamedQuery("GcmDevice.findCompanyDevices", GcmDeviceDTO.class);
        q.setParameter("companyID", companyID);
        List<GcmDevice> tList = q.getResultList();
        for (GcmDevice gcm : tList) {
            resp.getGcmDeviceList().add(new GcmDeviceDTO(gcm));
        }
        log.log(Level.OFF, "Company devices found: {0}", resp.getGcmDeviceList().size());
        return resp;
    }

    public static List<ProjectDTO> getProjectList(EntityManager em, Integer programmeID) {
        List<ProjectDTO> list = new ArrayList<>();
        Query q = em.createNamedQuery("Project.findByProgramme", Project.class);
        q.setParameter("programmeID", programmeID);
        List<Project> tList = q.getResultList();
        for (Project proj : tList) {
            ProjectDTO dto = new ProjectDTO(proj);
            list.add(dto);
        }

        return list;
    }

    public static ResponseDTO getMonitorList(EntityManager em, Integer companyID) {
        ResponseDTO resp = new ResponseDTO();
        Query q = em.createNamedQuery("Monitor.findByCompany", Monitor.class);
        q.setParameter("companyID", companyID);
        List<Monitor> tList = q.getResultList();
        resp.setMonitorList(new ArrayList<>());
        for (Monitor mon : tList) {
            resp.getMonitorList().add(new MonitorDTO(mon));
        }
        return resp;
    }

    

    public static ResponseDTO getProjectStatusPhotos(EntityManager em, Integer projectID) {
        ResponseDTO resp = new ResponseDTO();
        Query q = em.createNamedQuery("PhotoUpload.findByProject", PhotoUpload.class);
        q.setParameter("projectID", projectID);
        List<PhotoUpload> list = q.getResultList();
        resp.setPhotoUploadList(new ArrayList<>());
        for (PhotoUpload p : list) {
            resp.getPhotoUploadList().add(new PhotoUploadDTO(p));
        }

        q = em.createNamedQuery("ProjectTask.findByProject", ProjectTask.class);
        q.setParameter("projectID", projectID);
        List<ProjectTask> listp = q.getResultList();
        resp.setProjectTaskList(new ArrayList<>());

        Query q1 = em.createNamedQuery("ProjectTaskStatus.findByProject", ProjectTaskStatus.class);
        q1.setParameter("projectID", projectID);
        List<ProjectTaskStatus> tsList = q1.getResultList();
        for (ProjectTask p : listp) {
            ProjectTaskDTO pt = new ProjectTaskDTO(p);
            pt.setProjectTaskStatusList(new ArrayList<>());
            for (ProjectTaskStatus tts : tsList) {
                if (Objects.equals(tts.getProjectTask().getProjectTaskID(), pt.getProjectTaskID())) {
                    pt.getProjectTaskStatusList().add(new ProjectTaskStatusDTO(tts));
                }
            }

            resp.getProjectTaskList().add(pt);
        }

        return resp;
    }


    public static ResponseDTO getLocationTracksByStaff(EntityManager em, Integer companyStaffID) {
        ResponseDTO resp = new ResponseDTO();
        Query q = em.createNamedQuery("LocationTracker.findByStaff", LocationTracker.class);
        q.setParameter("companyStaffID", companyStaffID);
        List<LocationTracker> list = q.getResultList();
        resp.setLocationTrackerList(new ArrayList<>());
        for (LocationTracker t : list) {
            resp.getLocationTrackerList().add(new LocationTrackerDTO(t));
        }

        return resp;
    }

    public static ResponseDTO getLocationTracksByMonitorInPeriod(EntityManager em, Integer monitorID, Long df,
            Long dx) {
        Date dateFrom, dateTo;
        if (df == null) {
            DateTime dt = new DateTime();
            DateTime xx = dt.minusDays(1);
            dateFrom = xx.toDate();
            dateTo = dt.toDate();
            log.log(Level.INFO, "Get Location tracks from {0} to {1}",
                    new Object[]{dateFrom.toString(), dateTo.toString()});
        } else {
            dateFrom = new Date(df);
            dateTo = new Date(dx);
        }
        ResponseDTO resp = new ResponseDTO();
        Query q = em.createNamedQuery("LocationTracker.findByMonitorInPeriod", LocationTracker.class);
        q.setParameter("monitorID", monitorID);
        q.setParameter("dateFrom", dateFrom);
        q.setParameter("dateTo", dateTo);
        q.setMaxResults(5);
        List<LocationTracker> list = q.getResultList();
        resp.setLocationTrackerList(new ArrayList<>());
        for (LocationTracker t : list) {
            resp.getLocationTrackerList().add(new LocationTrackerDTO(t));
        }
        return resp;
    }

    public static ResponseDTO getLocationTracksByStaffInPeriod(EntityManager em, Integer companyStaffID, Long df,
            Long dx) {
        Date dateFrom, dateTo;
        if (df == null) {
            DateTime dt = new DateTime();
            DateTime xx = dt.minusDays(1);
            dateFrom = xx.toDate();
            dateTo = dt.toDate();
            log.log(Level.INFO, "Get Location tracks from {0} to {1}",
                    new Object[]{dateFrom.toString(), dateTo.toString()});
        } else {
            dateFrom = new Date(df);
            dateTo = new Date(dx);
        }
        ResponseDTO resp = new ResponseDTO();
        Query q = em.createNamedQuery("LocationTracker.findByStaffInPeriod", LocationTracker.class);
        q.setParameter("staffID", companyStaffID);
        q.setParameter("dateFrom", dateFrom);
        q.setParameter("dateTo", dateTo);
        List<LocationTracker> list = q.getResultList();
        resp.setLocationTrackerList(new ArrayList<>());
        for (LocationTracker t : list) {
            resp.getLocationTrackerList().add(new LocationTrackerDTO(t));
        }
        return resp;
    }

    public static ResponseDTO getLocationTracksByCompany(EntityManager em, Integer companyID, Long df, Long dx) {

        Date dateFrom, dateTo;
        if (df == null) {
            DateTime dt = new DateTime();
            DateTime xx = dt.minusDays(7);
            dateFrom = xx.toDate();
            dateTo = dt.toDate();
            log.log(Level.INFO, "Get Location tracks from {0} to {1}",
                    new Object[]{dateFrom.toString(), dateTo.toString()});
        } else {
            dateFrom = new Date(df);
            dateTo = new Date(dx);
        }
        ResponseDTO resp = new ResponseDTO();
        Query q = em.createNamedQuery("LocationTracker.findByCompanyInPeriod", LocationTracker.class);
        q.setParameter("companyID", companyID);
        q.setParameter("dateFrom", dateFrom);
        q.setParameter("dateTo", dateTo);

        List<LocationTracker> list = q.getResultList();
        resp.setLocationTrackerList(new ArrayList<>());
        list.stream().forEach((t) -> {
            resp.getLocationTrackerList().add(new LocationTrackerDTO(t));
        });

        log.log(Level.INFO, "LocationTrackers found, db: {0} out: {1}",
                new Object[]{list.size(), resp.getLocationTrackerList().size()});
        return resp;
    }

    public static ResponseDTO getPhotosByProject(EntityManager em, Integer projectID, 
            Date start, Date end) {
        ResponseDTO resp = new ResponseDTO();
        Query q = em.createNamedQuery("PhotoUpload.findByProjectInPeriod", PhotoUpload.class);
        q.setParameter("projectID", projectID);
        q.setParameter("start", start);
        q.setParameter("end", end);
        List<PhotoUpload> list = q.getResultList();
        resp.setPhotoUploadList(new ArrayList<>());
        for (PhotoUpload cp : list) {
            resp.getPhotoUploadList().add(new PhotoUploadDTO(cp));
        }

        return resp;
    }

    public static ResponseDTO getAllPhotosByProject(EntityManager em, Integer projectID) {
        ResponseDTO resp = new ResponseDTO();
        Query q = em.createNamedQuery("PhotoUpload.findByProject", PhotoUpload.class);
        q.setParameter("projectID", projectID);
        List<PhotoUpload> list = q.getResultList();
        resp.setPhotoUploadList(new ArrayList<>());
        for (PhotoUpload cp : list) {
            resp.getPhotoUploadList().add(new PhotoUploadDTO(cp));
        }
        System.out.println("**** found project photos: " + resp.getPhotoUploadList().size());

        return resp;
    }

    public static ResponseDTO getProjectStatus(EntityManager em, Integer projectID, Date start, Date end) {
        ResponseDTO resp = new ResponseDTO();

        Query q = em.createNamedQuery("ProjectTask.findByProject", ProjectTask.class);
        q.setParameter("projectID", projectID);
        List<ProjectTask> taskList = q.getResultList();
        resp.setProjectTaskList(new ArrayList<>());
        for (ProjectTask projectTask : taskList) {
            ProjectTaskDTO dto = new ProjectTaskDTO(projectTask);
            dto.setProjectTaskStatusList(new ArrayList<>());
            resp.getProjectTaskList().add(dto);
        }

        q = em.createNamedQuery("ProjectTaskStatus.findByProjectInPeriod", ProjectTaskStatus.class);
        q.setParameter("projectID", projectID);
        q.setParameter("start", start);
        q.setParameter("end", end);
        List<ProjectTaskStatus> taskStatusList = q.getResultList();

        System.out.println("ProjectTaskStatus found: " + taskStatusList.size());

        // get photos for every projectTask taken within period
        q = em.createNamedQuery("PhotoUpload.findByTaskInPeriod", PhotoUpload.class);
        q.setParameter("start", start);
        q.setParameter("end", end);
        for (ProjectTaskStatus projectTaskStatus : taskStatusList) {
            ProjectTaskStatusDTO dto = new ProjectTaskStatusDTO(projectTaskStatus);
            for (ProjectTaskDTO projectTask : resp.getProjectTaskList()) {
                if (Objects.equals(projectTask.getProjectTaskID(), dto.getProjectTaskID())) {
                    q.setParameter("projectTaskID", projectTask.getProjectTaskID());
                    List<PhotoUpload> pList = q.getResultList();
                    projectTask.setPhotoUploadList(new ArrayList<>());
                    for (PhotoUpload photoUpload : pList) {
                        projectTask.getPhotoUploadList().add(new PhotoUploadDTO(photoUpload));
                    }

                    projectTask.getProjectTaskStatusList().add(dto);
                }
            }
        }

        // get photos for the project - not task related, could be event photos
        q = em.createNamedQuery("PhotoUpload.findByProjectInPeriod", PhotoUpload.class);
        q.setParameter("projectID", projectID);
        q.setParameter("start", start);
        q.setParameter("end", end);
        List<PhotoUpload> pList = q.getResultList();
        System.out.println("photos found: " + pList.size());
        resp.setPhotoUploadList(new ArrayList<>());

        for (PhotoUpload photoUpload : pList) {
            resp.getPhotoUploadList().add(new PhotoUploadDTO(photoUpload));
        }

        resp.setStatusCount(taskStatusList.size());
        if (!taskStatusList.isEmpty()) {
            ProjectTaskStatusDTO dto = new ProjectTaskStatusDTO(taskStatusList.get(0));
            resp.setLastStatus(dto);
        }

        System.out.println("################# Hooray, project status done!");
        return resp;
    }

    private String getRandomPin() {
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

    public static ResponseDTO getPhotosByTask(EntityManager em, Integer projectTaskID) {
        ResponseDTO resp = new ResponseDTO();
        Query q = em.createNamedQuery("PhotoUpload.findByTask", PhotoUpload.class);
        q.setParameter("projectTaskID", projectTaskID);
        List<PhotoUpload> list = q.getResultList();
        resp.setPhotoUploadList(new ArrayList<>());
        for (PhotoUpload cp : list) {
            resp.getPhotoUploadList().add(new PhotoUploadDTO(cp));
        }

        return resp;
    }

    public static ResponseDTO getCompanyStaffList(EntityManager em, Integer companyID) throws DataException {
        ResponseDTO resp = new ResponseDTO();

        try {
            Query q = em.createNamedQuery("Staff.findByCompany", Staff.class);
            q.setParameter("companyID", companyID);
            List<Staff> sList = q.getResultList();
            resp.setStaffList(new ArrayList<>());
            for (Staff cs : sList) {
                StaffDTO dto = new StaffDTO(cs);
                dto.setPhotoUploadList(new ArrayList<>());
                q = em.createNamedQuery("PhotoUpload.findByStaff", PhotoUpload.class);
                q.setParameter("staffID", cs.getStaffID());
                List<PhotoUpload> pList = q.getResultList();
                for (PhotoUpload photoUpload : pList) {
                    dto.getPhotoUploadList().add(new PhotoUploadDTO(photoUpload));
                }
                resp.getStaffList().add(dto);
            }
            log.log(Level.INFO, "company staff found: {0}", resp.getStaffList().size());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed to get project data\n" + getErrorString(e));
        }

        return resp;
    }

    public static ResponseDTO getCompanyMonitorList(EntityManager em, Integer companyID) throws DataException {
        ResponseDTO resp = new ResponseDTO();

        try {
            Query q = em.createNamedQuery("Monitor.findByCompany", Monitor.class);
            q.setParameter("companyID", companyID);
            List<Monitor> sList = q.getResultList();
            resp.setMonitorList(new ArrayList<>());
            for (Monitor monitor : sList) {
                MonitorDTO dto = new MonitorDTO(monitor);
                dto.setPhotoUploadList(new ArrayList<>());

                q = em.createNamedQuery("PhotoUpload.findByMonitor", PhotoUpload.class);

                dto.setPhotoUploadList(new ArrayList<>());
                q.setParameter("monitorID", dto.getMonitorID());
                List<PhotoUpload> pList = q.getResultList();
                for (PhotoUpload photoUpload : pList) {
                    dto.getPhotoUploadList().add(new PhotoUploadDTO(photoUpload));
                }

                q = em.createNamedQuery("PhotoUpload.countProjectPhotosByMonitor", PhotoUpload.class);
                q.setParameter("monitorID", monitor.getMonitorID());
                Object obj = q.getSingleResult();
                Long count = (Long) obj;
                dto.setPhotoCount(count.intValue());

                q = em.createNamedQuery("ProjectTaskStatus.countByMonitor", ProjectTaskStatus.class);
                q.setParameter("monitorID", monitor.getMonitorID());
                Object objm = q.getSingleResult();
                Long countm = (Long) objm;
                dto.setStatusCount(countm.intValue());

                q = em.createNamedQuery("MonitorProject.countProjectsByMonitor", MonitorProject.class);
                q.setParameter("monitorID", monitor.getMonitorID());
                Object objx = q.getSingleResult();
                Long countx = (Long) objx;
                dto.setProjectCount(countx.intValue());

                resp.getMonitorList().add(dto);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed to get project data\n" + getErrorString(e));
        }

        return resp;
    }

    public static ResponseDTO getTaskStatusTypeList(EntityManager em, Integer companyID) throws DataException {
        ResponseDTO resp = new ResponseDTO();

        try {
            Query q = em.createNamedQuery("TaskStatusType.findByCompany", TaskStatusType.class);
            q.setParameter("companyID", companyID);
            List<TaskStatusType> sList = q.getResultList();
            resp.setTaskStatusTypeList(new ArrayList<>());
            for (TaskStatusType cs : sList) {
                resp.getTaskStatusTypeList().add(new TaskStatusTypeDTO(cs));
            }
            log.log(Level.INFO, "task status types found: {0}", sList.size());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed to get project data\n" + getErrorString(e));
        }

        return resp;
    }

    public static ResponseDTO getProjectStatusTypeList(EntityManager em, Integer companyID) throws DataException {
        ResponseDTO resp = new ResponseDTO();

        try {
            Query q = em.createNamedQuery("ProjectStatusType.findByCompany", ProjectStatusType.class);
            q.setParameter("companyID", companyID);
            List<ProjectStatusType> sList = q.getResultList();
            resp.setProjectStatusTypeList(new ArrayList<>());
            for (ProjectStatusType cs : sList) {
                resp.getProjectStatusTypeList().add(new ProjectStatusTypeDTO(cs));
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed to get project status list\n" + getErrorString(e));
        }

        return resp;
    }

    public static ResponseDTO getProjectTasks(EntityManager em, Integer projectID) throws DataException {
        ResponseDTO resp = new ResponseDTO();
        try {
            Query q = em.createNamedQuery("ProjectTask.findByProject", ProjectTask.class);
            q.setParameter("projectID", projectID);
            List<ProjectTask> pstList = q.getResultList();
            log.log(Level.INFO, "tasks found: {0}", pstList.size());
            resp.setProjectTaskList(new ArrayList<>());

            q = em.createNamedQuery("ProjectTaskStatus.findByProject", ProjectTaskStatus.class);
            q.setParameter("projectID", projectID);
            List<ProjectTaskStatus> ptsList = q.getResultList();
            for (ProjectTask projectTask : pstList) {
                ProjectTaskDTO dto = new ProjectTaskDTO(projectTask);
                for (ProjectTaskStatus pts : ptsList) {
                    if (Objects.equals(pts.getProjectTask().getProjectTaskID(), dto.getProjectTaskID())) {
                        dto.getProjectTaskStatusList().add(new ProjectTaskStatusDTO(pts));
                    }
                    dto.setStatusCount(dto.getProjectTaskStatusList().size());
                }
                resp.getProjectTaskList().add(dto);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed to get tasks\n" + getErrorString(e));
        }

        return resp;
    }

    public static ResponseDTO getProjectStatusData(EntityManager em, Integer projectID, int days) throws DataException {
        long s = System.currentTimeMillis();
        if (days == 0) {
            days = 30;
        }
        ResponseDTO resp = new ResponseDTO();
        try {
            Project p = em.find(Project.class, projectID);
            ProjectDTO project = new ProjectDTO(p);

            DateTime now = new DateTime();
            DateTime then = now.minusDays(days);
            then = then.withHourOfDay(0);
            then = then.withMinuteOfHour(0);
            then = then.withSecondOfMinute(0);

            project.setProjectTaskList(
                    ListUtil.getProjectStatus(em, projectID, then.toDate(), now.toDate()).getProjectTaskList());
            project.setPhotoUploadList(
                    getPhotosByProject(em, projectID, then.toDate(), now.toDate()).getPhotoUploadList());

            resp.setProjectList(new ArrayList<>());
            resp.getProjectList().add(project);

            long e = System.currentTimeMillis();
            log.log(Level.INFO, "############---------- project data retrieved: {0} seconds", Elapsed.getElapsed(s, e));
        } catch (OutOfMemoryError e) {
            log.log(Level.SEVERE, "Failed", e);
            throw new DataException("Failed to get project data: OUT OF MEMORY!\n");
        }

        return resp;
    }

    public static String getErrorString(Exception e) {
        StringBuilder sb = new StringBuilder();
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

        return sb.toString();
    }

   
    static final Logger log = Logger.getLogger(ListUtil.class.getSimpleName());
}
