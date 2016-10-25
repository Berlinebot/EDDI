package io.sls.utilities;

import io.sls.persistence.IResourceStore;
import org.jboss.resteasy.specimpl.ResponseBuilderImpl;
import org.jboss.resteasy.spi.NoLogWebApplicationException;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ginccc
 */
public class RestUtilities {
    private static final String versionQueryParam = "?version=";

    public static NoLogWebApplicationException createConflictException(String containerUri, IResourceStore.IResourceId currentId) {
        URI resourceUri = RestUtilities.createURI(containerUri, currentId.getId(), versionQueryParam, currentId.getVersion());

        ResponseBuilderImpl builder = new ResponseBuilderImpl();
        builder.status(Response.Status.CONFLICT);
        builder.entity(resourceUri.toString());

        return new NoLogWebApplicationException(builder.build());
    }

    public static URI createURI(Object... uriParts) {
        StringBuilder sb = new StringBuilder();

        for (Object uriPart : uriParts) {
            sb.append(uriPart.toString());
        }

        return URI.create(sb.toString());
    }

    public static IResourceStore.IResourceId extractResourceId(URI uri) {
        String uriString = uri.toString();

        String relativeUriString;
        if (uriString.contains("://")) {
            uriString = uriString.substring(uriString.indexOf("://") + 3, uriString.length());
            relativeUriString = uriString.substring(uriString.indexOf("/"), uriString.length());
        } else {
            relativeUriString = uriString;
        }

        if (relativeUriString.startsWith("/")) {
            relativeUriString = relativeUriString.substring(1, relativeUriString.length());
        }

        if (relativeUriString.endsWith("/")) {
            relativeUriString = relativeUriString.substring(0, relativeUriString.length() - 1);
        }


        String[] split = relativeUriString.split("/");
        String lastPartOfUri = split.length > 2 ? split[split.length - 1].split("\\?")[0] : null;
        final String id = isValidId(lastPartOfUri) ? lastPartOfUri : null;

        Integer queryParamVersion = 0;
        if (relativeUriString.contains(versionQueryParam)) {
            String queryParamsString = relativeUriString.split("\\?")[1];
            Map<String, String> queryMap = getQueryMap(queryParamsString);
            String versionString = queryMap.get("version");
            try {
                queryParamVersion = Integer.parseInt(versionString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Query param \"version\" must be a non-negative integer.");
            }
        }

        final Integer version = queryParamVersion;
        return new IResourceStore.IResourceId() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public Integer getVersion() {
                return version;
            }
        };
    }

    public static boolean isValidId(String s) {
        if (s == null || s.length() < 18) {
            return false;
        }

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c >= '0' && c <= '9') {
                continue;
            }

            if (c >= 'a' && c <= 'f') {
                continue;
            }

            if (c >= 'A' && c <= 'F') {
                continue;
            }

            return false;
        }

        return true;
    }

    public static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String[] keyValuePair = param.split("=");

            String name = null;
            if (keyValuePair.length > 0) {
                name = keyValuePair[0];
            }

            String value = null;
            if (keyValuePair.length > 1) {
                value = keyValuePair[1];
            }

            if (name != null && value != null) {
                map.put(name, value);
            }
        }
        return map;
    }
}