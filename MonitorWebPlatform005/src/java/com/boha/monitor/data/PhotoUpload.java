/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boha.monitor.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author aubreyM
 */
@Entity
@Table(name = "photoUpload")
@NamedQueries({
    @NamedQuery(name = "PhotoUpload.findByProject",
            query = "SELECT p FROM PhotoUpload p WHERE p.project.projectID = :projectID and p.projectTask IS NULL "
            + "ORDER BY p.dateTaken desc"),
    @NamedQuery(name = "PhotoUpload.findByProjectList",
            query = "SELECT p FROM PhotoUpload p WHERE p.project.projectID IN :projectList and p.projectTask IS NULL "
            + "ORDER BY p.dateTaken desc"),

    @NamedQuery(name = "PhotoUpload.findByProjectInPeriod",
            query = "SELECT p FROM PhotoUpload p WHERE p.project.projectID = :projectID and p.projectTask IS NULL "
            + "AND p.dateTaken BETWEEN :start AND :end ORDER BY p.dateTaken desc"),

    @NamedQuery(name = "PhotoUpload.findCompanyPhotosByPictureType",
            query = "SELECT p FROM PhotoUpload p WHERE  p.monitor.company.companyID = :companyID and p.pictureType IN :pictureTypes "
            + "ORDER BY p.dateTaken desc"),

    @NamedQuery(name = "PhotoUpload.findByMonitor",
            query = "SELECT p FROM PhotoUpload p WHERE p.monitor.monitorID = :monitorID and p.project IS NULL "
            + "ORDER BY p.dateTaken desc"),

    @NamedQuery(name = "PhotoUpload.findByMonitorList",
            query = "SELECT p FROM PhotoUpload p WHERE p.monitor.monitorID IN :monitorList and p.project IS NULL "
            + "ORDER BY p.dateTaken desc"),

    @NamedQuery(name = "PhotoUpload.countProjectPhotosByMonitor",
            query = "SELECT count(p) FROM PhotoUpload p WHERE p.monitor.monitorID = :monitorID and p.project IS NOT NULL"),

    @NamedQuery(name = "PhotoUpload.countProjectPhotosByStaff",
            query = "SELECT count(p) FROM PhotoUpload p WHERE p.staff.staffID = :staffID and p.project IS NOT NULL"),
    @NamedQuery(name = "PhotoUpload.findByStaff",
            query = "SELECT p FROM PhotoUpload p WHERE p.staff.staffID = :staffID and p.project IS NULL "
            + "ORDER BY p.dateTaken desc"),

    @NamedQuery(name = "PhotoUpload.findByMonitorProject",
            query = "SELECT p FROM PhotoUpload p, MonitorProject s WHERE "
            + "s.monitor.monitorID = :monitorID and "
            + "p.project.projectID = s.project.projectID "
            + "order by p.project.projectID "),

    @NamedQuery(name = "PhotoUpload.findByStaffProject",
            query = "SELECT p FROM PhotoUpload p, StaffProject s WHERE "
            + "s.staff.staffID = :staffID and "
            + "p.project.projectID = s.project.projectID "
            + "order by p.project.projectID "),

    @NamedQuery(name = "PhotoUpload.findByTaskInPeriod",
            query = "SELECT p FROM PhotoUpload p WHERE p.projectTask.projectTaskID = :projectTaskID "
            + "AND p.dateTaken BETWEEN :start AND :end ORDER BY p.dateTaken desc"),
    @NamedQuery(name = "PhotoUpload.findByTask",
            query = "SELECT p FROM PhotoUpload p WHERE p.projectTask.projectTaskID = :projectTaskID "
            + " ORDER BY p.dateTaken desc"),
    @NamedQuery(name = "PhotoUpload.findByTaskMonitor",
            query = "SELECT p FROM PhotoUpload p WHERE p.projectTask.projectTaskID = :projectTaskID and p.monitor.monitorID = :monitorID "
            + " ORDER BY p.dateTaken desc")
})
public class PhotoUpload implements Serializable {

    @OneToMany(mappedBy = "photoUpload")
    private List<PhotoTag> photoTagList;

    @Column(name = "marked")
    private Boolean marked;
    @Column(name = "sharedCount")
    private Integer sharedCount;
    @JoinColumn(name = "tenderCompanyID", referencedColumnName = "tenderCompanyID")
    @ManyToOne(fetch = FetchType.LAZY)
    private TenderCompany tenderCompanyID;

    @JoinColumn(name = "projectTaskStatusID", referencedColumnName = "projectTaskStatusID")
    @ManyToOne
    private ProjectTaskStatus projectTaskStatus;
    @Column(name = "statusColor")
    private Short statusColor;
    @Size(max = 400)
    @Column(name = "secureUrl")
    private String secureUrl;

    @Column(name = "width")
    private Integer width;
    @Column(name = "height")
    private Integer height;
    @Column(name = "bytes")
    private Integer bytes;
    @JoinColumn(name = "projectTaskID", referencedColumnName = "projectTaskID")
    @ManyToOne(fetch = FetchType.LAZY)
    private ProjectTask projectTask;
    @JoinColumn(name = "staffID", referencedColumnName = "staffID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Staff staff;
    @JoinColumn(name = "monitorID", referencedColumnName = "monitorID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Monitor monitor;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "photoUploadID")
    private Integer photoUploadID;

    @Basic(optional = false)
    @NotNull
    @Column(name = "pictureType")
    private int pictureType;
    @Basic(optional = false)
    @NotNull
    @Column(name = "dateTaken")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTaken;
    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "longitude")
    private Double longitude;
    @Column(name = "accuracy")
    private Float accuracy;
    @Size(max = 255)
    @Column(name = "uri")
    private String uri;
    @Column(name = "thumbFlag")
    private Integer thumbFlag;
    @Column(name = "dateUploaded")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateUploaded;
    @Size(max = 255)
    @Column(name = "thumbFilePath")
    private String thumbFilePath;
    @Column(name = "staffPicture")
    private Integer staffPicture;

    @JoinColumn(name = "projectID", referencedColumnName = "projectID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;

    public PhotoUpload() {
    }

    public PhotoUpload(Integer photoUploadID) {
        this.photoUploadID = photoUploadID;
    }

    public PhotoUpload(Integer photoUploadID, int pictureType, Date dateTaken) {
        this.photoUploadID = photoUploadID;
        this.pictureType = pictureType;
        this.dateTaken = dateTaken;
    }

    public Integer getPhotoUploadID() {
        return photoUploadID;
    }

    public void setPhotoUploadID(Integer photoUploadID) {
        this.photoUploadID = photoUploadID;
    }

    public int getPictureType() {
        return pictureType;
    }

    public void setPictureType(int pictureType) {
        this.pictureType = pictureType;
    }

    public Date getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(Date dateTaken) {
        this.dateTaken = dateTaken;
    }

    public ProjectTaskStatus getProjectTaskStatus() {
        return projectTaskStatus;
    }

    public void setProjectTaskStatus(ProjectTaskStatus projectTaskStatus) {
        this.projectTaskStatus = projectTaskStatus;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Float accuracy) {
        this.accuracy = accuracy;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Integer getThumbFlag() {
        return thumbFlag;
    }

    public void setThumbFlag(Integer thumbFlag) {
        this.thumbFlag = thumbFlag;
    }

    public Date getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(Date dateUploaded) {
        this.dateUploaded = dateUploaded;
    }

    public String getThumbFilePath() {
        return thumbFilePath;
    }

    public void setThumbFilePath(String thumbFilePath) {
        this.thumbFilePath = thumbFilePath;
    }

    public Integer getStaffPicture() {
        return staffPicture;
    }

    public void setStaffPicture(Integer staffPicture) {
        this.staffPicture = staffPicture;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (photoUploadID != null ? photoUploadID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PhotoUpload)) {
            return false;
        }
        PhotoUpload other = (PhotoUpload) object;
        if ((this.photoUploadID == null && other.photoUploadID != null) || (this.photoUploadID != null && !this.photoUploadID.equals(other.photoUploadID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.boha.monitor.data.PhotoUpload[ photoUploadID=" + photoUploadID + " ]";
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    public ProjectTask getProjectTask() {
        return projectTask;
    }

    public void setProjectTask(ProjectTask projectTask) {
        this.projectTask = projectTask;
    }

    public String getSecureUrl() {
        return secureUrl;
    }

    public void setSecureUrl(String secureUrl) {
        this.secureUrl = secureUrl;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getBytes() {
        return bytes;
    }

    public void setBytes(Integer bytes) {
        this.bytes = bytes;
    }

    public Short getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(Short statusColor) {
        this.statusColor = statusColor;
    }

    public Boolean getMarked() {
        return marked;
    }

    public void setMarked(Boolean marked) {
        this.marked = marked;
    }

    public Integer getSharedCount() {
        return sharedCount;
    }

    public void setSharedCount(Integer sharedCount) {
        this.sharedCount = sharedCount;
    }

    public TenderCompany getTenderCompanyID() {
        return tenderCompanyID;
    }

    public void setTenderCompanyID(TenderCompany tenderCompanyID) {
        this.tenderCompanyID = tenderCompanyID;
    }

    @XmlTransient
    public List<PhotoTag> getPhotoTagList() {
        return photoTagList;
    }

    public void setPhotoTagList(List<PhotoTag> photoTagList) {
        this.photoTagList = photoTagList;
    }

}
