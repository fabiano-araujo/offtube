package br.com.developer.fabiano.ytdownload.adapters;

import java.util.ArrayList;

import br.com.developer.fabiano.ytdownload.models.Link;

/**
 * Created by Fabiano on 09/10/2016.
 */

public class Control {
    public static  ArrayList<Link> getLinkDownload(String s){
        ArrayList<Link> links = new ArrayList<>();
        while (true) {
            int index = s.indexOf("formatButton\" id=\"");
            if (index != -1){
                Link link = new Link();
                int size = "formatButton\" id=\"".length();
                int indexFim = s.indexOf("\"",index+size);

                link.link = "http://www.saveitoffline.com/"+s.substring(index+size,indexFim);

                index = s.indexOf("</i>");
                indexFim = s.indexOf("</button");
                link.label = s.substring(index+"</i>".length(),indexFim);
                links.add(link);
                s = s.substring(indexFim+1,s.length()-1);
            }else{
                return links;
            }
        }
    }
}
