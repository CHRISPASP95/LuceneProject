package lucene;

public class PrecisionRecall {

    double precision;
    double recall;

    public  PrecisionRecall()
    {

    }


    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getRecall() {
        return recall;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }
}
