import java.nio.file.Path;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.nio.file.Files;

public class GetPart implements Runnable{

    private int threadId;
    private URL url;
    private Path path;
    private Boolean range = false;

    private long offset;
    private long end;

    GetPart(int threadId, URL url, Path path){
        this.url = url;
        this.path = path;
        this.threadId = threadId;
    }
    
    GetPart(int threadId, URL url, Path path, long offset, long end){
        this.url = url;
        this.path = path;
        this.range = true;
        this.offset = offset;
        this.end = end;
        this.threadId = threadId;
    }

    @Override
    public void run(){
        long start1 = System.currentTimeMillis();
        OutputStream out = null;
        InputStream in = null;

        try{
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            if(range){
                con.setRequestProperty("Range", "bytes="+offset+"-"+end);
            }

            if(!Files.exists(path)){
                Files.createFile(path);
            }

            byte[] buf = new byte[1024];

            in = con.getInputStream();
            out = new FileOutputStream(path.toFile());

            int n;

            while((n = in.read(buf, 0, buf.length))!=-1){
                out.write(buf, 0, n);
            }
            
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null) {
                    in.close();
                }
            } catch (IOException ex) {
                //Ignore
            }
        }

        long end1 = System.currentTimeMillis();

        System.out.println("Thread#"+this.threadId+" Elapsed time : "+(end1-start1)+" ms");
    }
}