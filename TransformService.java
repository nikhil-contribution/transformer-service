package com.transform;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TransformService {

    private Object payload;
    private Object config;

    public TransformService(Object payload, Object config) {
        this.payload = payload;
        this.config = config;
    }

    public Object transform() throws Exception {

        if (this.config == null) {
            return this.payload;
        }
        if (this.payload == null) {
            return null;
        }
        return traverseConfig(this.config);

    }

    @SuppressWarnings("unchecked")
    private Object traverseConfig(Object input) throws Exception {

        if (input instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) input;

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                map.put(key, traverseConfig(map.get(key)));
            }
        }
        if (input instanceof List) {
            List<Object> list = (List<Object>) input;
            for (int i = 0; i < list.size(); i++) {
                list.set(i, traverseConfig(list.get(i)));
            }
        }
        if (input instanceof String) {
            input = getActVal(input);
        }
        return input;

    }

    @SuppressWarnings("unchecked")
    private Object getActVal(Object input) throws Exception {

        if ("*".equals(input)) {
            return input;
        }

        String[] paths = ((String) input).split("\\.");

        if (input instanceof Map && ((Map)input).get(paths[0]) == null)
            throw new Exception("Invalid path");
        return findVal(input, paths, this.payload);

    }

    private Object findVal(Object input, String[] paths, Object payload) {
        Object payloadObj = payload;
        for (String eachKey : paths) {
            if (payloadObj instanceof Map) {
                payloadObj = ((Map<String, Object>) payloadObj).containsKey(eachKey)
                        ? ((Map<String, Object>) payloadObj).get(eachKey) : null;
            } else if (payloadObj instanceof List) {
                try {
                    payloadObj = "*".equals(eachKey) ? payloadObj
                            : ((List<Object>) payloadObj).get(Integer.parseInt(eachKey));
                } catch (NumberFormatException e) {
                    List<Object> vals = new LinkedList<>();
                    boolean isCommaAvbl = eachKey.contains(",");
                    for (Object eachObj : ((List<Object>) payloadObj)) {
                        if(isCommaAvbl) {
                            String[] attributes = eachKey.split(",");
                            Map<String, Object> val = new HashMap<>();
                            for (String key : attributes) {
                                String[] alias = key.split(":");
                                val.put(alias.length > 1 ? alias[1] : key, ((Map<String, Object>)eachObj).get(alias[0]));
                            }
                            vals.add(val);
                        } else {
                            vals.add(((Map<String, Object>)eachObj).get(eachKey));
                        }
                    }
                    payloadObj = vals;
                }
            }
        }
        return payloadObj;
    }

    public static void main(String[] args) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Raju");
        payload.put("gender", "Male");
        payload.put("address", "Hyderabad");
        List<Object> history = new LinkedList<>();
        Map<String, String> history1 = new HashMap<>();
        history1.put("history-key", "history1-val");
        history1.put("event-key", "event1-val");
        history1.put("player", "player1-val");
        Map<String, String> history2 = new HashMap<>();
        history2.put("history-key", "history2-val");
        history2.put("event-key", "event2-val");
        history2.put("player", "player2-val");
        Map<String, String> history3 = new HashMap<>();
        history3.put("history-key", "history3-val");
        history3.put("event-key", "event3-val");
        history3.put("player", "player3-val");
        history.add(history1);
        history.add(history2);
        history.add(history3);
        payload.put("history", history);

        Map<String, Object> config = new HashMap<>();
        config.put("fullName", "name");
        config.put("sex", "gender");
        Map<String, String> address = new HashMap<>();
        address.put("locality", "address");
        List<Map<String, String>> addressList = new LinkedList<>();
        addressList.add(address);
        config.put("address", addressList);
        config.put("first-history", "history.*.history-key");
        config.put("events", "history.*.event-key:eventId,player");
        config.put("allEvents", "history.*");
        config.put("test", "test");

        TransformService service = new TransformService(payload, config);
        try {
            System.out.println(service.transform());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
