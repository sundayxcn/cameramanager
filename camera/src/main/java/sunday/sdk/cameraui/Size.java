package sunday.sdk.cameraui;

/**
 * @author apple
 * @decrption
 * @data 2019-07-12
 **/
public class Size {
    public int width;
    public int height;
    public Size(int width,int height){
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
