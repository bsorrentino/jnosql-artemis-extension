/*
 *  Copyright (c) 2017 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.jnosql.artemis.orientdb.document;


import org.jnosql.artemis.Converters;
import org.jnosql.artemis.document.AbstractDocumentTemplate;
import org.jnosql.artemis.document.DocumentEntityConverter;
import org.jnosql.artemis.document.DocumentEventPersistManager;
import org.jnosql.artemis.document.DocumentWorkflow;
import org.jnosql.artemis.reflection.ClassRepresentations;
import org.jnosql.diana.api.document.DocumentCollectionManager;
import org.jnosql.diana.api.document.DocumentEntity;
import org.jnosql.diana.api.document.DocumentQuery;
import org.jnosql.diana.orientdb.document.OrientDBDocumentCollectionManager;
import org.jnosql.diana.orientdb.document.OrientDBLiveCallback;
import org.jnosql.diana.orientdb.document.OrientDBLiveCallbackBuilder;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The Default implementation of {@link OrientDBTemplate}
 */
@Typed(OrientDBTemplate.class)
class DefaultOrientDBTemplate extends AbstractDocumentTemplate
        implements OrientDBTemplate {

    private Instance<OrientDBDocumentCollectionManager> manager;

    private DocumentEntityConverter converter;

    private DocumentWorkflow flow;

    private DocumentEventPersistManager persistManager;

    private ClassRepresentations classRepresentations;

    private Converters converters;

    @Inject
    DefaultOrientDBTemplate(Instance<OrientDBDocumentCollectionManager> manager,
                            DocumentEntityConverter converter, DocumentWorkflow flow,
                            DocumentEventPersistManager persistManager,
                            ClassRepresentations classRepresentations,
                            Converters converters) {

        this.manager = manager;
        this.converter = converter;
        this.flow = flow;
        this.persistManager = persistManager;
        this.classRepresentations = classRepresentations;
        this.converters = converters;
    }

    DefaultOrientDBTemplate() {
    }

    @Override
    protected DocumentEntityConverter getConverter() {
        return converter;
    }

    @Override
    protected DocumentCollectionManager getManager() {
        return manager.get();
    }

    @Override
    protected DocumentWorkflow getWorkflow() {
        return flow;
    }

    @Override
    protected DocumentEventPersistManager getPersistManager() {
        return persistManager;
    }

    @Override
    protected ClassRepresentations getClassRepresentations() {
        return classRepresentations;
    }

    @Override
    protected Converters getConverters() {
        return converters;
    }

    @Override
    public <T> List<T> sql(String query, Object... params) {
        return manager.get().sql(query, params).stream().map(converter::toEntity)
                .map(e -> (T) e)
                .collect(Collectors.toList());
    }

    @Override
    public <T> List<T> sql(String query, Map<String, Object> params) {
        return manager.get().sql(query, params).stream().map(converter::toEntity)
                .map(e -> (T) e)
                .collect(Collectors.toList());
    }

    @Override
    public <T> void live(DocumentQuery query, OrientDBLiveCallback<T> callBacks) {
        Objects.requireNonNull(query, "query is required");
        Objects.requireNonNull(callBacks, "callBacks is required");
        manager.get().live(query, bindCallbacks(callBacks));
    }

    @Override
    public <T> void live(String query, OrientDBLiveCallback<T> callBacks, Object... params) {
        Objects.requireNonNull(query, "query is required");
        Objects.requireNonNull(callBacks, "callBack is required");
        manager.get().live(query, bindCallbacks(callBacks), params);
    }

    private <T> OrientDBLiveCallback<DocumentEntity> bindCallbacks(OrientDBLiveCallback<T> callBacks) {
        return OrientDBLiveCallbackBuilder.builder()
                .onCreate(d -> callBacks.getCreateCallback().ifPresent(callback -> callback.accept(converter.toEntity(d))))
                .onUpdate(d -> callBacks.getUpdateCallback().ifPresent(callback -> callback.accept(converter.toEntity(d))))
                .onDelete(d -> callBacks.getDeleteCallback().ifPresent(callback -> callback.accept(converter.toEntity(d))))
                .build();
    }
}
