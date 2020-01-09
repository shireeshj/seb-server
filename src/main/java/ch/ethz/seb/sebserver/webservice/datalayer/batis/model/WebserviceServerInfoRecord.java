package ch.ethz.seb.sebserver.webservice.datalayer.batis.model;

import javax.annotation.Generated;

public class WebserviceServerInfoRecord {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.826+01:00", comments="Source field: webservice_server_info.id")
    private Long id;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.826+01:00", comments="Source field: webservice_server_info.uuid")
    private String uuid;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.826+01:00", comments="Source field: webservice_server_info.service_address")
    private String serviceAddress;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.826+01:00", comments="Source Table: webservice_server_info")
    public WebserviceServerInfoRecord(Long id, String uuid, String serviceAddress) {
        this.id = id;
        this.uuid = uuid;
        this.serviceAddress = serviceAddress;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.826+01:00", comments="Source field: webservice_server_info.id")
    public Long getId() {
        return id;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.826+01:00", comments="Source field: webservice_server_info.uuid")
    public String getUuid() {
        return uuid;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.826+01:00", comments="Source field: webservice_server_info.service_address")
    public String getServiceAddress() {
        return serviceAddress;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table webservice_server_info
     *
     * @mbg.generated Thu Jan 09 10:29:16 CET 2020
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", uuid=").append(uuid);
        sb.append(", serviceAddress=").append(serviceAddress);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table webservice_server_info
     *
     * @mbg.generated Thu Jan 09 10:29:16 CET 2020
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        WebserviceServerInfoRecord other = (WebserviceServerInfoRecord) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUuid() == null ? other.getUuid() == null : this.getUuid().equals(other.getUuid()))
            && (this.getServiceAddress() == null ? other.getServiceAddress() == null : this.getServiceAddress().equals(other.getServiceAddress()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table webservice_server_info
     *
     * @mbg.generated Thu Jan 09 10:29:16 CET 2020
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUuid() == null) ? 0 : getUuid().hashCode());
        result = prime * result + ((getServiceAddress() == null) ? 0 : getServiceAddress().hashCode());
        return result;
    }
}