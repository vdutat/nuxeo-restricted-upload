package org.nuxeo.addons;


import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.RecoverableClientException;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.utils.BlobsExtractor;

public class RestrictedUploadEventListener implements EventListener {

    protected List<String> handled = Stream.of(DocumentEventTypes.ABOUT_TO_CREATE, DocumentEventTypes.BEFORE_DOC_UPDATE).collect(Collectors.toList());
    
    protected final BlobsExtractor blobExtractor = new BlobsExtractor();

    private static final Log log = LogFactory.getLog(RestrictedUploadEventListener.class);

    @Override
    public void handleEvent(Event event) {
        if (!acceptEvent(event)) {
            return;
        }
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }
        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();
        for (Property prop : blobExtractor.getBlobsProperties(doc)) {
            if (prop.isDirty()) {
                if (!isUploadAllowed(event, prop)) {
                    reject(event, prop);
                }
            }
        }
    }

    protected void reject(Event event, Property prop) {
        event.markRollBack();
        EventContext ctx = event.getContext();
        String msg = String.format("%s not allowed to update %s",ctx.getPrincipal().getName(), prop.getField().getName().toString());
        throw new RecoverableClientException(msg, msg, null);
    }
    
    protected boolean acceptEvent(Event event) {
        return handled.contains(event.getName());
    }

    protected boolean isUploadAllowed(Event event, Property prop) {
        // TODO
        return false;
    }

}
