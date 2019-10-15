package org.thevlad.web.esignature.docusign;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;

public class DSHelper {
    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * This method read bytes content from resource
     *
     * @param fileName - resource path
     * @return - return bytes array
     * @throws IOException
     */
    public static byte[] readContent(String fileName) throws IOException {

        InputStream is = DSHelper.class.getResourceAsStream("/edoctemplates/"+fileName);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;

        byte[] data = new byte[1024];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }

    /**
     * This method printing pretty json format
     * @param arg - any object to be written as string
     */
    public static void printPrettyJSON(Object arg) {
        try {
            String jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arg);
            System.out.println("Results:");
            System.out.println(jsonInString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
