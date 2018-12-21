package sunday.sdk.camera;
public interface PreviewRepertory {
    void addPreview(Preview preview);

    Preview getLastPreview();

    void clear();
}
