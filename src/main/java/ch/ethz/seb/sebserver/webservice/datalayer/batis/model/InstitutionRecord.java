package ch.ethz.seb.sebserver.webservice.datalayer.batis.model;

import javax.annotation.Generated;

public class InstitutionRecord {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.809+01:00", comments="Source field: institution.id")
    private Long id;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.809+01:00", comments="Source field: institution.name")
    private String name;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.809+01:00", comments="Source field: institution.url_suffix")
    private String urlSuffix;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.809+01:00", comments="Source field: institution.theme_name")
    private String themeName;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.809+01:00", comments="Source field: institution.active")
    private Integer active;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.809+01:00", comments="Source field: institution.logo_image")
    private String logoImage;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.808+01:00", comments="Source Table: institution")
    public InstitutionRecord(Long id, String name, String urlSuffix, String themeName, Integer active, String logoImage) {
        this.id = id;
        this.name = name;
        this.urlSuffix = urlSuffix;
        this.themeName = themeName;
        this.active = active;
        this.logoImage = logoImage;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.809+01:00", comments="Source field: institution.id")
    public Long getId() {
        return id;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.809+01:00", comments="Source field: institution.name")
    public String getName() {
        return name;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.809+01:00", comments="Source field: institution.url_suffix")
    public String getUrlSuffix() {
        return urlSuffix;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.809+01:00", comments="Source field: institution.theme_name")
    public String getThemeName() {
        return themeName;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.809+01:00", comments="Source field: institution.active")
    public Integer getActive() {
        return active;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-01-09T10:29:16.809+01:00", comments="Source field: institution.logo_image")
    public String getLogoImage() {
        return logoImage;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table institution
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
        sb.append(", name=").append(name);
        sb.append(", urlSuffix=").append(urlSuffix);
        sb.append(", themeName=").append(themeName);
        sb.append(", active=").append(active);
        sb.append(", logoImage=").append(logoImage);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table institution
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
        InstitutionRecord other = (InstitutionRecord) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getUrlSuffix() == null ? other.getUrlSuffix() == null : this.getUrlSuffix().equals(other.getUrlSuffix()))
            && (this.getThemeName() == null ? other.getThemeName() == null : this.getThemeName().equals(other.getThemeName()))
            && (this.getActive() == null ? other.getActive() == null : this.getActive().equals(other.getActive()))
            && (this.getLogoImage() == null ? other.getLogoImage() == null : this.getLogoImage().equals(other.getLogoImage()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table institution
     *
     * @mbg.generated Thu Jan 09 10:29:16 CET 2020
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getUrlSuffix() == null) ? 0 : getUrlSuffix().hashCode());
        result = prime * result + ((getThemeName() == null) ? 0 : getThemeName().hashCode());
        result = prime * result + ((getActive() == null) ? 0 : getActive().hashCode());
        result = prime * result + ((getLogoImage() == null) ? 0 : getLogoImage().hashCode());
        return result;
    }
}