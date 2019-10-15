package org.thevlad.web.esignature.docusign;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.docusign.esign.model.SignHere;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class consists of static constant configuration value that used by docusign examples
 * this members are set once access any member first time.
 * configuration can be loaded from environment variables or from properties file config.properties.
 * loading order is respectively first trying to load environment variables if not found will try
 * loaf from config.properties file.
 */
public final class DSConfig {

    public static final String CLIENT_ID;
    public static final String IMPERSONATED_USER_GUID;
    public static final String TARGET_ACCOUNT_ID;
    public static final String OAUTH_REDIRECT_URI = "https://www.docusign.com";
    public static final String DS_APP_URL;
    public static final String SIGNER_EMAIL;
    public static final String SIGNER_NAME;
    public static final String CC_EMAIL;
    public static final String CC_NAME;
    public static final String PRIVATE_KEY;
    public static final String AUTHENTICATION_URL = "https://account-d.docusign.com";
    public static final String DS_AUTH_SERVER;
    public static final String API = "restapi/v2";
    public static final String PERMISSION_SCOPES = "signature%20impersonation";
    public static final String JWT_SCOPE = "signature";
    public static final String DS_SIGN_HERE_CONFIG_FILE;
    private static final Map<String,SignHere> SIGN_HERE_CONFIG_MAP= new HashMap<String, SignHere>();
    private static final ObjectMapper mapper = new ObjectMapper();
    		
    public static final String AUD () {
        if(DS_AUTH_SERVER != null && DS_AUTH_SERVER.startsWith("https://"))
            return DS_AUTH_SERVER.substring(8);
        else if(DS_AUTH_SERVER != null && DS_AUTH_SERVER.startsWith("http://"))
            return DS_AUTH_SERVER.substring(7);

        return DS_AUTH_SERVER;
    }

    static {
        // Try load from environment variables
        Map<String, String> envConfig = loadFromEnv();
        Map<String, String> propsConfig = loadFromProperties();

        CLIENT_ID = fetchValue("DS_CLIENT_ID", envConfig, propsConfig);
        IMPERSONATED_USER_GUID = fetchValue("DS_IMPERSONATED_USER_GUID", envConfig, propsConfig);
        TARGET_ACCOUNT_ID = fetchValue("DS_TARGET_ACCOUNT_ID", envConfig, propsConfig);
        DS_APP_URL = fetchValue("DS_APP_URL", envConfig, propsConfig);
        SIGNER_EMAIL = fetchValue("DS_SIGNER_1_EMAIL", envConfig, propsConfig);
        SIGNER_NAME = fetchValue("DS_SIGNER_1_NAME", envConfig, propsConfig);
        CC_EMAIL = fetchValue("DS_CC_1_EMAIL", envConfig, propsConfig);
        CC_NAME = fetchValue("DS_CC_1_NAME", envConfig, propsConfig);
        PRIVATE_KEY = fetchValue("RSA_PRIVATE_KEY", envConfig, propsConfig);
        DS_AUTH_SERVER = fetchValue("DS_AUTH_SERVER", envConfig, propsConfig); // use account.docusign.com for production
        DS_SIGN_HERE_CONFIG_FILE = fetchValue("DS_SIGN_HERE_CONFIG_FILE", envConfig, propsConfig);
        initSignHere();
    }

    /**
     * fetch configuration value by key.
     *
     * @param envConfig preloaded configuration key/value map
     * @param name key of value
     * @return value as string or default empty string
     */
	private static String fetchValue(String name, Map<String, String> envConfig, Map<String, String> propsConfig) {
		String val = envConfig.get(name);

		if (val != null) {
			return val;
		} else {
			val = propsConfig.get(name);
		}
		if ("DS_TARGET_ACCOUNT_ID".equals(name) && "FALSE".equals(val)) {
			return null;
		}
		return ((val != null) ? val : "");
	}

    private static void initSignHere() {
		try {
			JsonNode root = mapper.readTree(DSConfig.class.getClassLoader().getResourceAsStream(DS_SIGN_HERE_CONFIG_FILE));
			for (JsonNode jsonNode : root) {
				String name = jsonNode.get("name").textValue();
				JsonNode bodyNode = jsonNode.get("body");
				SignHere signHere = mapper.treeToValue(bodyNode, SignHere.class);
				SIGN_HERE_CONFIG_MAP.put(name, signHere);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

    public static SignHere getSignHereForTemplate(String name) {
    	return SIGN_HERE_CONFIG_MAP.get(name);
    }
    
	/**
     * This method check if environment variables exists and load it into Map
     *
     * @return Map of key/value of environment variables if exists otherwise, return null
     */
    private static Map<String, String> loadFromEnv() {
        String clientId = System.getenv("DS_CLIENT_ID");

        if (clientId != null && clientId.length() > 0) {
            return System.getenv();
        }

        return null;
    }

    /**
     * This method load properties located in config.properties file in the working directory.
     *
     * @return Map of key/value of properties
     */
    private static Map<String, String> loadFromProperties() {
        Properties properties = new Properties();
        InputStream input = null;

        try {
            input = DSConfig.class.getResourceAsStream("/dsconfig.properties");
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("can not load configuration file", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    throw new RuntimeException("error occurs will closing input stream: ", e);
                }
            }
        }

        Set<Map.Entry<Object, Object>> set = properties.entrySet();
        Map<String, String> mapFromSet = new HashMap<String, String>();

        for (Map.Entry<Object, Object> entry : set) {
            mapFromSet.put((String) entry.getKey(), (String) entry.getValue());
        }

        return mapFromSet;
    }
}
