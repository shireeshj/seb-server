/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gui.service.page.impl;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import ch.ethz.seb.sebserver.gbl.profile.GuiProfile;
import ch.ethz.seb.sebserver.gui.service.i18n.I18nSupport;
import ch.ethz.seb.sebserver.gui.service.page.ComposerService;
import ch.ethz.seb.sebserver.gui.service.page.PageContext;
import ch.ethz.seb.sebserver.gui.service.page.PageDefinition;
import ch.ethz.seb.sebserver.gui.service.page.TemplateComposer;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.RestService;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.auth.AuthorizationContextHolder;
import ch.ethz.seb.sebserver.gui.widget.WidgetFactory;

@Lazy
@Service
@GuiProfile
public class ComposerServiceImpl implements ComposerService {

    private static final Logger log = LoggerFactory.getLogger(ComposerServiceImpl.class);

    // TODO configurable
    private final Class<? extends PageDefinition> loginPageType = DefaultLoginPage.class;
    private final Class<? extends PageDefinition> mainPageType = DefaultMainPage.class;

    final AuthorizationContextHolder authorizationContextHolder;
    private final RestService restService;
    private final I18nSupport i18nSupport;
    private final Map<String, TemplateComposer> composer;
    private final Map<String, PageDefinition> pages;

    public ComposerServiceImpl(
            final AuthorizationContextHolder authorizationContextHolder,
            final RestService restService,
            final I18nSupport i18nSupport,
            final Collection<TemplateComposer> composer,
            final Collection<PageDefinition> pageDefinitions) {

        this.authorizationContextHolder = authorizationContextHolder;
        this.restService = restService;
        this.i18nSupport = i18nSupport;
        this.composer = composer
                .stream()
                .collect(Collectors.toMap(
                        comp -> comp.getClass().getName(),
                        Function.identity()));

        this.pages = pageDefinitions
                .stream()
                .collect(Collectors.toMap(
                        page -> page.getClass().getName(),
                        Function.identity()));
    }

    @Override
    public PageDefinition mainPage() {
        return this.pages.get(this.mainPageType.getName());
    }

    @Override
    public PageDefinition loginPage() {
        return this.pages.get(this.loginPageType.getName());
    }

    @Override
    public boolean validate(final String composerName, final PageContext pageContext) {
        if (!this.composer.containsKey(composerName)) {
            return false;
        }

        return this.composer
                .get(composerName)
                .validate(pageContext);
    }

    @Override
    public void compose(
            final Class<? extends TemplateComposer> composerType,
            final PageContext pageContext) {

        compose(composerType.getName(), pageContext);
    }

    @Override
    public void compose(
            final String name,
            final PageContext pageContext) {

        if (!this.composer.containsKey(name)) {
            log.error("No TemplateComposer with name: " + name + " found. Check Spring confiuration and beans");
            return;
        }

        final TemplateComposer composer = this.composer.get(name);

        if (composer.validate(pageContext)) {

            WidgetFactory.clearComposite(pageContext.getParent());

            try {
                composer.compose(pageContext);
            } catch (final Exception e) {
                log.warn("Failed to compose: {}, pageContext: {}", name, pageContext, e);
            }

            try {
                pageContext.getParent().layout();
            } catch (final Exception e) {
                log.warn("Failed to layout new composition: {}, pageContext: {}", name, pageContext, e);
            }

        } else {
            log.error(
                    "Invalid or missing mandatory attributes to handle compose request of ViewComposer: {} pageContext: {}",
                    name,
                    pageContext);
        }

    }

    @Override
    public void composePage(
            final PageDefinition pageDefinition,
            final Composite root) {

        compose(
                pageDefinition.composer(),
                pageDefinition.applyPageContext(createPageContext(root)));
    }

    @Override
    public void composePage(
            final Class<? extends PageDefinition> pageType,
            final Composite root) {

        final String pageName = pageType.getName();
        if (!this.pages.containsKey(pageName)) {
            log.error("Unknown page with name: {}", pageName);
            return;
        }

        final PageDefinition pageDefinition = this.pages.get(pageName);
        compose(
                pageDefinition.composer(),
                pageDefinition.applyPageContext(createPageContext(root)));
    }

    @Override
    public void loadLoginPage(final Composite parent) {
        composePage(this.loginPageType, parent);
    }

    @Override
    public void loadMainPage(final Composite parent) {
        composePage(this.mainPageType, parent);
    }

    private PageContext createPageContext(final Composite root) {
        return new PageContextImpl(this.restService, this.i18nSupport, this, root, root, null);
    }

}
