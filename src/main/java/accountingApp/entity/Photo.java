package accountingApp.entity;

public class Photo extends MultipartFileAdapter {

    private long photoId;
    private byte[] content;
    private String contentType;


    public Photo(long photoId,
                 byte[] content,
                 String contentType) {
        this.photoId = photoId;
        this.content = content;
        this.contentType = contentType;

    }

    public Photo(byte[] content,
                 String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    public long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(long photoId) {
        this.photoId = photoId;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
