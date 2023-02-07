
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.URLConnection;
import java.util.Map;
import java.util.Random;
import java.util.List;


public class Nhunt{

    public static int THRESH_HOLD = 2097152; //2MB

    public static String checkHeader(String urlString, String headerKey) {
		String value = null;
		try {
			String url = urlString;
			URL urlObj = new URL(url);
			URLConnection urlCon = urlObj.openConnection();
			 
			Map<String, List<String>> map = urlCon.getHeaderFields();
			 
			 
			for (String key : map.keySet()) {
			 
			    List<String> values = map.get(key);
			 
			    for (String aValue : values) {
			        
			        if(key!=null && key.equals(headerKey)==true) {
			        	return aValue;
			        	
			        }
			    }
			}
		} catch (Exception ex) {
			//
		}
		
		return value;
	}

    public static String randomString(){
        Random rand = new Random();
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder("");
        for(int i=0;i<18;i++){
            if(i==6 || i==14) sb.append("-");
            sb.append(alphabet.charAt(rand.nextInt(26)));
        }
        return sb.toString();
    }

    public static void main(String...args) {
        //Get the current working directory
        String currentDirectory = Paths.get("").toAbsolutePath().toString();
        
        String urlString = null;
        Path destination = null;

        //Creating n Executor service with a fixed thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        
        try{
            urlString = args[0];

            Path fileName = Path.of(urlString);
            URL url = new URL(urlString);
            
            try{
                destination = Path.of(currentDirectory ,args[1]);
            } catch (IndexOutOfBoundsException ex) {
                destination = Path.of(currentDirectory, fileName.getName(fileName.getNameCount()-1).toString());
            }

            String acceptRanges = checkHeader(urlString, "Accept-Ranges");

            if(acceptRanges!=null && acceptRanges.equals("bytes")){
                int contentLength = Integer.parseInt(checkHeader(urlString, "Content-Length"));
                int N = (int) Math.ceil(contentLength/THRESH_HOLD);

                for(int i=1;i<=N;i++){
                    Path temp = Path.of(currentDirectory,randomString()+"_"+i);
                    if(i==N){
                        executorService.submit(new GetPart(i,url, temp, ((i-1)*THRESH_HOLD)+1, contentLength));
                    }
                    else if(i==1){
                        executorService.submit(new GetPart(i,url, temp, 0, i*THRESH_HOLD));
                    }
                    else{
                        executorService.submit(new GetPart(i,url, temp, ((i-1)*THRESH_HOLD)+1, i*THRESH_HOLD));
                    }
                }
            } else{
    
                executorService.submit(new GetPart(1,url, destination));
            }

            executorService.shutdown();

        } catch (IndexOutOfBoundsException ex) {
            System.out.println("Usage : Nhunt <URL>");
        } catch (MalformedURLException mex) {
            System.out.println("Error : Malformed Url");
        }
    }
}