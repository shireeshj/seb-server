/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gui.content;

import java.util.function.Function;

import org.eclipse.swt.widgets.Composite;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import ch.ethz.seb.sebserver.gbl.Constants;
import ch.ethz.seb.sebserver.gbl.api.EntityType;
import ch.ethz.seb.sebserver.gbl.model.Domain;
import ch.ethz.seb.sebserver.gbl.model.exam.Exam;
import ch.ethz.seb.sebserver.gbl.model.exam.QuizData;
import ch.ethz.seb.sebserver.gbl.model.institution.LmsSetup;
import ch.ethz.seb.sebserver.gbl.profile.GuiProfile;
import ch.ethz.seb.sebserver.gui.content.action.ActionDefinition;
import ch.ethz.seb.sebserver.gui.service.ResourceService;
import ch.ethz.seb.sebserver.gui.service.i18n.I18nSupport;
import ch.ethz.seb.sebserver.gui.service.i18n.LocTextKey;
import ch.ethz.seb.sebserver.gui.service.page.PageContext;
import ch.ethz.seb.sebserver.gui.service.page.PageMessageException;
import ch.ethz.seb.sebserver.gui.service.page.PageService;
import ch.ethz.seb.sebserver.gui.service.page.PageService.PageActionBuilder;
import ch.ethz.seb.sebserver.gui.service.page.TemplateComposer;
import ch.ethz.seb.sebserver.gui.service.page.impl.PageAction;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.RestService;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.exam.GetExams;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.auth.CurrentUser;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.auth.CurrentUser.GrantCheck;
import ch.ethz.seb.sebserver.gui.table.ColumnDefinition;
import ch.ethz.seb.sebserver.gui.table.ColumnDefinition.TableFilterAttribute;
import ch.ethz.seb.sebserver.gui.table.EntityTable;
import ch.ethz.seb.sebserver.gui.table.TableFilter.CriteriaType;
import ch.ethz.seb.sebserver.gui.widget.WidgetFactory;

@Lazy
@Component
@GuiProfile
public class ExamList implements TemplateComposer {

    private final PageService pageService;
    private final ResourceService resourceService;
    private final int pageSize;

    private final static LocTextKey emptySelectionTextKey =
            new LocTextKey("sebserver.exam.info.pleaseSelect");
    private final static LocTextKey columnTitleLmsSetupKey =
            new LocTextKey("sebserver.exam.list.column.lmssetup");
    private final static LocTextKey columnTitleNameKey =
            new LocTextKey("sebserver.exam.list.column.name");
    private final static LocTextKey columnTitleTypeKey =
            new LocTextKey("sebserver.exam.list.column.type");
    private final static LocTextKey noModifyOfOutDatedExams =
            new LocTextKey("sebserver.exam.list.modify.out.dated");

    private final TableFilterAttribute lmsFilter;
    private final TableFilterAttribute nameFilter =
            new TableFilterAttribute(CriteriaType.TEXT, QuizData.FILTER_ATTR_NAME);
    private final TableFilterAttribute startTimeFilter =
            new TableFilterAttribute(CriteriaType.DATE, QuizData.FILTER_ATTR_START_TIME);

    protected ExamList(
            final PageService pageService,
            final ResourceService resourceService,
            @Value("${sebserver.gui.list.page.size}") final Integer pageSize) {

        this.pageService = pageService;
        this.resourceService = resourceService;
        this.pageSize = (pageSize != null) ? pageSize : 20;

        this.lmsFilter = new TableFilterAttribute(
                CriteriaType.SINGLE_SELECTION,
                LmsSetup.FILTER_ATTR_LMS_SETUP,
                this.resourceService::lmsSetupResource);
    }

    @Override
    public void compose(final PageContext pageContext) {

        final WidgetFactory widgetFactory = this.pageService.getWidgetFactory();
        final CurrentUser currentUser = this.resourceService.getCurrentUser();
        final RestService restService = this.resourceService.getRestService();
        final I18nSupport i18nSupport = this.resourceService.getI18nSupport();

        // content page layout with title
        final Composite content = widgetFactory.defaultPageLayout(
                pageContext.getParent(),
                new LocTextKey("sebserver.exam.list.title"));

        final PageActionBuilder actionBuilder = this.pageService.pageActionBuilder(pageContext.clearEntityKeys());

        // table
        final EntityTable<Exam> table =
                this.pageService.entityTableBuilder(restService.getRestCall(GetExams.class))
                        .withEmptyMessage(new LocTextKey("sebserver.exam.list.empty"))
                        .withPaging(this.pageSize)
                        .withColumn(new ColumnDefinition<>(
                                Domain.EXAM.ATTR_LMS_SETUP_ID,
                                columnTitleLmsSetupKey,
                                examLmsSetupNameFunction(this.resourceService),
                                this.lmsFilter,
                                false))
                        .withColumn(new ColumnDefinition<>(
                                QuizData.QUIZ_ATTR_NAME,
                                columnTitleNameKey,
                                Exam::getName,
                                this.nameFilter,
                                true))
                        .withColumn(new ColumnDefinition<>(
                                QuizData.QUIZ_ATTR_START_TIME,
                                new LocTextKey(
                                        "sebserver.exam.list.column.starttime",
                                        i18nSupport.getUsersTimeZoneTitleSuffix()),
                                Exam::getStartTime,
                                this.startTimeFilter,
                                true))
                        .withColumn(new ColumnDefinition<>(
                                Domain.EXAM.ATTR_TYPE,
                                columnTitleTypeKey,
                                this::examTypeName,
                                true))
                        .withDefaultAction(actionBuilder
                                .newAction(ActionDefinition.EXAM_VIEW_FROM_LIST)
                                .create())
                        .compose(content);

        // propagate content actions to action-pane
        final GrantCheck userGrant = currentUser.grantCheck(EntityType.EXAM);
        actionBuilder

                .newAction(ActionDefinition.EXAM_IMPORT)
                .publishIf(userGrant::im)

                .newAction(ActionDefinition.EXAM_VIEW_FROM_LIST)
                .withSelect(table::getSelection, PageAction::applySingleSelection, emptySelectionTextKey)
                .publishIf(table::hasAnyContent)

                .newAction(ActionDefinition.EXAM_MODIFY_FROM_LIST)
                .withSelect(
                        table::getSelection,
                        action -> this.modifyExam(action, table),
                        emptySelectionTextKey)
                .publishIf(() -> userGrant.im() && table.hasAnyContent());

    }

    private PageAction modifyExam(final PageAction action, final EntityTable<Exam> table) {
        final Exam exam = table.getSelectedROWData();

        if (exam.startTime != null) {
            final DateTime now = DateTime.now(DateTimeZone.UTC);
            if (exam.startTime.isBefore(now)) {
                throw new PageMessageException(noModifyOfOutDatedExams);
            }
        }

        return action.withEntityKey(action.getSingleSelection());
    }

    private static Function<Exam, String> examLmsSetupNameFunction(final ResourceService resourceService) {
        return exam -> resourceService.getLmsSetupNameFunction()
                .apply(String.valueOf(exam.lmsSetupId));
    }

    private String examTypeName(final Exam exam) {
        if (exam.type == null) {
            return Constants.EMPTY_NOTE;
        }

        return this.resourceService.getI18nSupport()
                .getText("sebserver.exam.type." + exam.type.name());
    }

}