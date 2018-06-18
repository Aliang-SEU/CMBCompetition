public class DenseMartrix {

    private float[][] data;
    private int nrows;
    private int ncols;

    public DenseMartrix() {
    }

    public DenseMartrix(float[][] data, int nrows, int ncols) {
        this.data = data;
        this.nrows = nrows;
        this.ncols = ncols;
    }

    public float[][] getData() {
        return data;
    }

    public void setData(float[][] data) {
        this.data = data;
    }

    public int getNrows() {
        return nrows;
    }

    public void setNrows(int nrows) {
        this.nrows = nrows;
    }

    public int getNcols() {
        return ncols;
    }

    public void setNcols(int ncols) {
        this.ncols = ncols;
    }

    public float[][] getRowData(int begin, int end){
        if(begin < 0 || begin > end || end > nrows){
            return null;
        }
        float[][] result = new float[end - begin][ncols];
        for(int i = begin; i < end; i++){
            System.arraycopy(data[i], 0, result[i - begin], 0, ncols);
        }
        return result;

    }
}
