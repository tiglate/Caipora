package br.dev.ampliar.caipora.model;

@SuppressWarnings("unused")
public class SoftwareDeployedDTO {
    private Integer softwareId;
    private String code;
    private String name;
    private DeployedDTO dev;
    private DeployedDTO qa;
    private DeployedDTO uat;
    private DeployedDTO prod;
    private DeployedDTO dr;

    public SoftwareDeployedDTO(Integer softwareId, String softwareCode, String softwareName) {
        this.softwareId = softwareId;
        this.code = softwareCode;
        this.name = softwareName;
    }

    public Integer getSoftwareId() {
        return softwareId;
    }

    public void setSoftwareId(Integer softwareId) {
        this.softwareId = softwareId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeployedDTO getDev() {
        return dev;
    }

    public void setDev(DeployedDTO dev) {
        this.dev = dev;
    }

    public DeployedDTO getQa() {
        return qa;
    }

    public void setQa(DeployedDTO qa) {
        this.qa = qa;
    }

    public DeployedDTO getUat() {
        return uat;
    }

    public void setUat(DeployedDTO uat) {
        this.uat = uat;
    }

    public DeployedDTO getProd() {
        return prod;
    }

    public void setProd(DeployedDTO prod) {
        this.prod = prod;
    }

    public DeployedDTO getDr() {
        return dr;
    }

    public void setDr(DeployedDTO dr) {
        this.dr = dr;
    }
}