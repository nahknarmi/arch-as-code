package net.trilogy.arch.adapter.structurizr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.Workspace;
import com.structurizr.model.Container;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.View;
import com.structurizr.view.ViewSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;

public class StructurizrViewsMapper {

    public static final String STRUCTURIZR_VIEWS_JSON = "structurizrViews.json";

    private static final Logger logger = LogManager.getLogger(StructurizrViewsMapper.class);
    private final File documentRoot;

    public StructurizrViewsMapper(File documentRoot) {
        this.documentRoot = documentRoot;
    }

    public void loadAndSetViews(Workspace workspace) {
        try {
            ViewSet viewSet = loadStructurizrViews();
            addViewsWithReferencedObjects(workspace, viewSet);
        } catch (IOException e) {
            logger.error("Failed to load views", e);
            throw new RuntimeException("Failed to load views", e);
        }
    }

    private ViewSet loadStructurizrViews() throws IOException {
        File manifestFile = new File(documentRoot.getCanonicalPath() + File.separator + STRUCTURIZR_VIEWS_JSON);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(manifestFile, ViewSet.class);
    }

    private void addViewsWithReferencedObjects(Workspace workspace, ViewSet viewSet) {
        viewSet.getSystemLandscapeViews().forEach(lv -> setSoftwareSystem(workspace, lv));

        viewSet.getSystemContextViews().forEach(sv -> setSoftwareSystem(workspace, sv));

        viewSet.getContainerViews().forEach(cv -> setSoftwareSystem(workspace, cv));

        viewSet.getComponentViews().forEach(cv -> {
            if (cv.getSoftwareSystem() == null && cv.getSoftwareSystemId() != null) {
                setSoftwareSystem(workspace, cv);
                Container container = cv.getSoftwareSystem().getContainerWithId(cv.getContainerId());
                setOnModel(cv, container);
            }
            if (cv.getContainer() == null && cv.getContainerId() != null) {
                Optional<SoftwareSystem> system = workspace.getModel().getSoftwareSystems().stream().filter(s -> s.getContainerWithId(cv.getContainerId()) != null).findFirst();
                if (system.isPresent()) {
                    SoftwareSystem softwareSystem = system.get();
                    setOnModel(cv, softwareSystem);
                    Container container = softwareSystem.getContainerWithId(cv.getContainerId());
                    setOnModel(cv, container);
                }
            }
        });

        viewSet.getDeploymentViews().forEach(dv -> setSoftwareSystem(workspace, dv));

        viewSet.getDynamicViews().forEach(dv -> setSoftwareSystem(workspace, dv));

        setOnModel(workspace, viewSet, "setViews");
    }

    private void setSoftwareSystem(Workspace workspace, View view) {
        if (view.getSoftwareSystem() == null && view.getSoftwareSystemId() != null) {
            SoftwareSystem softwareSystem = workspace.getModel().getSoftwareSystemWithId(view.getSoftwareSystemId());
            setOnModel(view, softwareSystem);
        }
    }

    private void setOnModel(Object model, Object objectToSet) {
        if (objectToSet != null) {
            this.setOnModel(model, objectToSet, "set" + objectToSet.getClass().getSimpleName());
        }
    }

    private void setOnModel(Object model, Object objectToSet, String methodName) {
        try {
            Method setViews = findSetter(model.getClass(), objectToSet, methodName);
            setViews.setAccessible(true);
            setViews.invoke(model, objectToSet);
        } catch (Exception e) {
            logger.error("Failed to set object:" + objectToSet + " using:" + methodName + " on model:" + model, e);
        }
    }

    private Method findSetter(Class model, Object objectToSet, String methodName) {
        try {
            return model.getDeclaredMethod(methodName, objectToSet.getClass());
        } catch (NoSuchMethodException e) {
            return findSetter(model.getSuperclass(), objectToSet, methodName);
        }
    }
}
