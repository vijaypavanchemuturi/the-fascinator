package au.edu.usq.fascinator.transformer.jsonVelocity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.usq.fascinator.common.JsonConfigHelper;

/**
 * Utility calss for JsonVelocity Transformer
 * 
 * @author Linda Octalina
 * 
 */
public class Util {
    /**
     * Getlist method to get the values of key from the sourceMap
     * 
     * @param sourceMap Map container
     * @param baseKey field to search
     * @return list of value based on baseKey
     */
    
    /** Logger */
    static Logger log = LoggerFactory
            .getLogger(JsonVelocityTransformer.class);
    
    public Map<String, Object> getList(Map<String, Object> sourceMap,
            String baseKey) {
        if (!baseKey.endsWith(".")) {
            baseKey = baseKey + ".";
        }
        SortedMap<String, Object> valueMap = new TreeMap<String, Object>();
        Map<String, Object> data;

        for (String key : sourceMap.keySet()) {
            if (key.startsWith(baseKey)) {
                
                String value = sourceMap.get(key).toString();
                String field = baseKey;
                if (key.length() >= baseKey.length())
                    field = key.substring(baseKey.length(), key.length());
                
                String index = field;
                if (field.indexOf(".")>0)
                    index = field.substring(0, field.indexOf("."));
                
                if (valueMap.containsKey(index))
                    data = (Map<String, Object>) valueMap.get(index);
                else { 
                    data = new LinkedHashMap<String, Object>();
                    valueMap.put(index, data);
                }
                 
                if (value.length() == 1)
                    value = String.valueOf(value.charAt(0));
                
                data.put(field.substring(field.indexOf(".") + 1, field.length()), value);
                
            }
        }
        
        log.info("{}: {}", baseKey, valueMap);
        return valueMap;
    }
    
    public String getW3CDateTime (String dateTime) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat odf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        
        Date date = sdf.parse(dateTime);
        return odf.format(date);
    }
}