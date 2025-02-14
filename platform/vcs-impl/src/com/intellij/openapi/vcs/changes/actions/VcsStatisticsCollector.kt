// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.openapi.vcs.changes.actions

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.eventLog.events.StringEventField
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.AbstractVcs
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.Change

class VcsStatisticsCollector : CounterUsagesCollector() {
  companion object {
    @JvmField
    val GROUP = EventLogGroup("vcs", 10)

    @JvmField
    val UPDATE_ACTIVITY = GROUP.registerIdeActivity("update")

    @JvmField
    val FETCH_ACTIVITY = GROUP.registerIdeActivity("fetch")

    @JvmField
    val COMMIT_ACTIVITY = GROUP.registerIdeActivity("commit")

    private val WAS_UPDATING_BEFORE = EventFields.Boolean("wasUpdatingBefore")
    private val CHANGES_DELTA = EventFields.Int("changesDelta")
    private val UNVERSIONED_DELTA = EventFields.Int("unversionedDelta")
    private val CHANGES_VIEW_REFRESH = GROUP.registerVarargEvent("changes.view.refresh", WAS_UPDATING_BEFORE, CHANGES_DELTA,
                                                                 UNVERSIONED_DELTA)

    val NON_MODAL_COMMIT_STATE_CHANGED = GROUP.registerEvent("non.modal.commit.state.changed", EventFields.Enabled)
    val NON_MODAL_COMMIT_PROMOTION_SHOWN = GROUP.registerEvent("non.modal.commit.promotion.shown")
    val NON_MODAL_COMMIT_PROMOTION_ACCEPTED = GROUP.registerEvent("non.modal.commit.promotion.accepted")
    val NON_MODAL_COMMIT_PROMOTION_REJECTED = GROUP.registerEvent("non.modal.commit.promotion.rejected")

    @JvmField
    val CLONE = GROUP.registerEvent("clone.invoked", EventFields.Class("clone_dialog_extension"))

    @JvmField
    val CLONED_PROJECT_OPENED = GROUP.registerEvent("cloned.project.opened")

    private val VCS_FIELD = object : StringEventField("vcs") {
      override val validationRule: List<String> get() = listOf("{enum#vcs}")
    }

    private val CLM_REFRESH = GROUP.registerEvent("clm_refresh",
                                                  EventFields.DurationMs,
                                                  VCS_FIELD,
                                                  EventFields.Boolean("is_full_refresh"))

    @JvmStatic
    fun logRefreshActionPerformed(project: Project,
                                  changesBefore: Collection<Change>,
                                  changesAfter: Collection<Change>,
                                  unversionedBefore: Collection<FilePath>,
                                  unversionedAfter: Collection<FilePath>,
                                  wasUpdatingBefore: Boolean) {
      val changesDelta = computeDelta(changesBefore, changesAfter)
      val unversionedDelta = computeDelta(unversionedBefore, unversionedAfter)

      CHANGES_VIEW_REFRESH.log(project,
                               WAS_UPDATING_BEFORE.with(wasUpdatingBefore),
                               CHANGES_DELTA.with(changesDelta),
                               UNVERSIONED_DELTA.with(unversionedDelta))
    }

    @JvmStatic
    fun logClmRefreshed(startMs: Long, vcs: AbstractVcs, everythingDirty: Boolean) {
      CLM_REFRESH.log(System.currentTimeMillis() - startMs, vcs.name, everythingDirty)
    }

    private fun <T> computeDelta(before: Collection<T>, after: Collection<T>): Int {
      val beforeSet = before.toHashSet()
      val afterSet = after.toHashSet()

      var result = 0
      for (value in beforeSet) {
        if (!afterSet.contains(value)) {
          result++
        }
      }
      for (value in afterSet) {
        if (!beforeSet.contains(value)) {
          result++
        }
      }
      return result
    }
  }

  override fun getGroup(): EventLogGroup {
    return GROUP
  }
}