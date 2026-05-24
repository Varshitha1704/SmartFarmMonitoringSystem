public class DiseaseResult {
    public final String diseaseName;
    public final String cause;
    public final String pesticide;
    public final String prevention;
    public final String engineStatus;

    public DiseaseResult(String diseaseName, String cause, String pesticide, String prevention) {
        this(diseaseName, cause, pesticide, prevention, "");
    }

    public DiseaseResult(String diseaseName, String cause, String pesticide, String prevention, String engineStatus) {
        this.diseaseName = diseaseName;
        this.cause = cause;
        this.pesticide = pesticide;
        this.prevention = prevention;
        this.engineStatus = engineStatus;
    }
}
