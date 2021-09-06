package debt.utils;

public enum FileTypes {
    png("image/png") ,
    jpg("image/jpeg") ,
    mp4("video/mp4") ,
    other("other") ;

    String content;
    FileTypes(String content){
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
