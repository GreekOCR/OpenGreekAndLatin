/**
 * @author federico.boschetti.73[AT]gmail.com
 */

package eu.himeros.transcoder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransGamera {

    /**
     * General purpose transcoder
     */

    private Hashtable<String, String> directHt;
    private Hashtable<String, String> reverseHt;
    private Hashtable<String, String> ht;    
    private int maxLen = 0;
    private Pattern pattern=null;
    private String regexp=null;

    public TransGamera() {}
    
    public TransGamera(String transFileName) {
        setTranscoder(transFileName);
    }

    public TransGamera(Hashtable<String,String> ht){
        this.ht=directHt=ht;
        makeReverseHt();
    }

    public TransGamera(Hashtable<String,String> ht, boolean reverse){
        this.ht=directHt=ht;
        makeReverseHt();
        reverse(reverse);
    }
    
    public TransGamera(String transFileName, boolean reverse){
        setTranscoder(transFileName);
        reverse(reverse);
    }

    public void reverse(boolean reverse){
        if(reverse){
            ht=reverseHt;
        }else{
            ht=directHt;
        }
    }

    public void reverse(){
        ht=reverseHt;
    }
    
    public void setTranscoder(String transFileName){
        try {
            /* read code file, transform utf8s in chars and put
            into a hashtable the sequences of char codes */
            String codeFrom;
            String codeTo;
            File transFile = new File(transFileName);
            //BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(transFile), "UTF-8"));
	    BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(transFileName)));
            directHt = new Hashtable<String, String>();
            reverseHt = new Hashtable<String, String>();
            String line=null;
            String lineLeft = "";
            String lineRight = "";
            String[] items=null;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("//")) {
                    items=line.split("\t");
                    lineLeft=items[0];
                    lineRight=items[1];
                    if (lineLeft.contains("\\u")) {
                        codeFrom = utf8sToChars(lineLeft);
                    } else {
                        codeFrom = lineLeft;
                    }
                    if (codeFrom.length() > maxLen) {
                        maxLen = codeFrom.length();
                    }
                    if(lineRight.contains("\\u")){
                        codeTo = utf8sToChars(lineRight);
                    }else{
                        codeTo=lineRight;
                    }
                    directHt.put(codeFrom, codeTo);
                    reverseHt.put(codeTo, codeFrom);
                }
            }
            ht=directHt;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTranscoder(String transFileName, boolean reverse){
            setTranscoder(transFileName);
            reverse(reverse);
        }

    public void setTranscoder(Hashtable<String,String> ht){
        this.ht=directHt=ht;
        makeReverseHt();
    }

    public Hashtable<String,String> getTranscoder(){
        return ht;
    }

    public Hashtable<String,String> getDirectTranscoder(){
        return directHt;
    }

    public Hashtable<String,String> getReverseTranscoder(){
        return reverseHt;
    }

    public String getCodes() {
        String res="";
        Set<String> keys=ht.keySet();
        String key="";
        for(Iterator<String> i=keys.iterator();i.hasNext();){
            key=i.next();
            res+=key+"\t"+ht.get(key)+"\n";
        }
        return res;
    }

    public String decode(String key){
        return ht.get(key);
    }

    private String utf8sToChars(String str) {
        if(str.equals("\\uxxxx")) return "";
        StringBuffer result =new StringBuffer();
        Pattern p=Pattern.compile("(?:\\\\)u([0-9A-Fa-f]{4})");
        Matcher m=p.matcher(str);
        while(m.find()){
            m.appendReplacement(result,""+((char)Long.parseLong(m.group(1),16)));
        }
        m.appendTail(result);
        return result.toString();
    }
    
    public String parse(String inStr){
        if(inStr==null||inStr.length()==0) return "";
        inStr += addSpaces(maxLen);
        String frag = "";
        String outStr = "";
        String code = "";
        int len = inStr.length();
        int iLeft = 0;
        int iRight = maxLen;
        while(iRight <= len){
            while(iRight > iLeft){
                frag = inStr.substring(iLeft, iRight);
                code = ht.get(frag);
                if (code != null) {
                    outStr += code;
                    break;
                }
                iRight--;
            }
            if(iRight==iLeft){
                iRight+=1;
                outStr+=frag;
            }
            iLeft = iRight;
            iRight += maxLen;
        }
        if(outStr.length()>1){
            return outStr.substring(0,outStr.length()-1);
        }else return outStr;
    }

    private String addSpaces(int n) {
        String res = "";
        for (int i = 0; i < n; i++) {
            res += " ";
        }
        return res;
    }

    public String parse(String inStr, String regexp){
         if(pattern==null||(regexp!=null&&regexp.equals(this.regexp))){
             this.regexp=regexp;
             pattern=Pattern.compile(regexp);
         }
        Matcher matcher=pattern.matcher(inStr);
        StringBuffer sb=new StringBuffer();
        while(matcher.find()){
            if(matcher.groupCount()==3){
                matcher.appendReplacement(sb, matcher.group(1)+parse(matcher.group(2)+matcher.group(3)));
            }else{
                matcher.appendReplacement(sb, parse(matcher.group(1)));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public String parse(String inStr, String startTagRegexp, String endTagRegexp, boolean del){
        if(!del){
            startTagRegexp="("+startTagRegexp+")";
            endTagRegexp="("+endTagRegexp+")";
        }
        return parse(inStr, startTagRegexp+"(.*?)"+endTagRegexp);
    }

    private void makeReverseHt(){
        String val=null;
        String key=null;
        Enumeration<String> keys=directHt.keys();
        while(keys.hasMoreElements()){
            key=keys.nextElement();
            val=directHt.get(key);
            reverseHt.put(val, key);
        }
    }
    
    private static void printUsage(){
            System.out.println("usage: transgamera <transFile> <  <STDIN>");
    }

    public static void main(String[] args){
        try{
            //BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "UTF-8"));
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            //BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8"));
            String line=null;
            TransGamera trans=new TransGamera("/eu/himeros/resources/transcoders/comb2u.txt");
            while((line=br.readLine())!=null){
		line=line.replaceAll("([αειηουω])\\u02bc([^ ])","$1\u0313$2");
		line=line.replaceAll("([αειηουω])([βγδζθκλμνξπρσςτφχψ])([\\u0300-\\u036F]{1,2})","$1$3$2");
                line=line.replaceAll("([\u0300\u0301\u0342])([\u0313\u0314])","$2$1");
                line=line.replaceAll("([βγδζθκλμνξπρσςτφχψ])([\\u0300-\\u036F]{1,2})","$1");
                line=trans.parse(line);
                //bw.write(line);bw.newLine();
		System.out.println(line);
	    }
            //bw.close();
        }catch(Exception ex){
            printUsage();
            ex.printStackTrace();
        }
    }

}