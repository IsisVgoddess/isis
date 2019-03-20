/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.core.runtime.services.i18n.po;

import static org.apache.isis.config.internal._Config.getConfiguration;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.isis.applib.services.i18n.LocaleProvider;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.i18n.TranslationsResolver;
import org.apache.isis.commons.internal.context._Context;

import lombok.AccessLevel;
import lombok.Getter;

@ApplicationScoped //FIXME [2033] cannot provision with @Singleton
//@Slf4j
public class TranslationServicePo implements TranslationService {

    public static final String KEY_PO_MODE = "isis.services.translation.po.mode";

    /**
     * Defaults to writer mode because the service won't have been init'd while the metamodel is being instantiated,
     * and we want to ensure that we capture all requests for translation.
     */
    private PoAbstract po = new PoDisabled(this); //TODO [2033] was ... new PoWriter(this);


    // -- init, shutdown

    //TODO [2033] @PostConstruct
    public void init() {

        final String translationMode = getConfiguration().getString(KEY_PO_MODE);

        final boolean translationDisabled = TranslationService.Mode.DISABLED.matches(translationMode);
        if(translationDisabled) {
            // switch to disabled mode
            po = new PoDisabled(this);
            return;
        }

        if(getLocaleProvider() == null || getTranslationsResolver() == null) {
            // remain in write mode
            return;
        }

        final boolean prototypeOrTest = isPrototypeOrTest();

        final boolean forceRead = TranslationService.Mode.READ.matches(translationMode);

        if(prototypeOrTest && !forceRead) {
            // remain in write mode
            return;
        }

        // switch to read mode
        final PoReader poReader = new PoReader(this);
        poReader.init(); //TODO [2033] why does this require a Wicket application ?
        po = poReader;
    }

    protected boolean isPrototypeOrTest() {
        return _Context.isPrototyping();
    }

    @PreDestroy
    public void shutdown() {
        po.shutdown();
    }



    @Override
    public String translate(final String context, final String text) {
        return po.translate(context, text);
    }

    @Override
    public String translate(final String context, final String singularText, final String pluralText, final int num) {
        return po.translate(context, singularText, pluralText, num);
    }

    @Override
    public Mode getMode() {
        return po.getMode();
    }

    /**
     * Not API
     */
    public String toPot() {
        if (!getMode().isWrite()) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        ((PoWriter)po).toPot(buf);
        return buf.toString();
    }


    /**
     * Not API
     */
    void clearCache() {
        if (!getMode().isRead()) {
            return;
        }
        ((PoReader)po).clearCache();
    }

    private PoReader previousPoReader;
    private PoWriter previousPoWriter;

    /**
     * Not API
     */
    public void toggleMode() {
        if(getMode().isRead()) {
            previousPoReader = (PoReader) po;
            if (previousPoWriter != null) {
                po = previousPoWriter;
            } else {
                po = new PoWriter(this);
            }
        } else {
            previousPoWriter = (PoWriter)po;
            if(previousPoReader != null) {
                previousPoReader.clearCache();
                po = previousPoReader;
            } else {
                final PoReader poReader = new PoReader(this);
                poReader.init();
                po = poReader;
            }
        }
    }

    // //////////////////////////////////////

    @Inject @Getter(AccessLevel.PACKAGE) 
    private TranslationsResolver translationsResolver;

    @Inject @Getter(AccessLevel.PACKAGE)
    private LocaleProvider localeProvider;
    

}
