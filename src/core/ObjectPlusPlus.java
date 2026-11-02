package core;

import java.util.HashMap;
import java.util.Map;

public abstract class ObjectPlusPlus extends ObjectPlus {
    private Map<String, Map<Object, ObjectPlusPlus>> links = new HashMap<>();

    public ObjectPlusPlus() {
        super();
    }

    public boolean isLink(String roleName, ObjectPlusPlus targetObject) {
        Map<Object, ObjectPlusPlus> objectLink;

        if (roleName == null || targetObject == null) {
            throw new IllegalArgumentException("Role name and target object cannot be null.");
        }

        if (!links.containsKey(roleName)) {
            return false;
        }

        objectLink = links.get(roleName);

        return objectLink.containsValue(targetObject);
    }

    public void addLink(String roleName, String reverseRoleName, ObjectPlusPlus targetObject) throws Exception {
        if (roleName == null || roleName.isBlank()) {
            throw new Exception("Role name cannot be empty.");
        }
        if (reverseRoleName == null || reverseRoleName.isBlank()) {
            throw new Exception("Reverse role name cannot be empty.");
        }
        if (targetObject == null) {
            throw new Exception("Target object cannot be null.");
        }
        if (isLink(roleName, targetObject)) {
            return;
        }

        addLinkInternal(roleName, targetObject);
        targetObject.addLinkInternal(reverseRoleName, this);
    }

    public boolean anyLink(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("Role name cannot be empty.");
        }
        Map<Object, ObjectPlusPlus> objectLinks = links.get(roleName);
        return objectLinks != null && !objectLinks.isEmpty();
    }

    private void addLinkInternal(String roleName, ObjectPlusPlus targetObject) {
        Map<Object, ObjectPlusPlus> objectLinks =
                links.computeIfAbsent(roleName, k -> new HashMap<>());
        objectLinks.put(targetObject, targetObject);
    }
}
