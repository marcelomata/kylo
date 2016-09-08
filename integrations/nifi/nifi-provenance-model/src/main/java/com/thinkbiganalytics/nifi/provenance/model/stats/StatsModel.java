package com.thinkbiganalytics.nifi.provenance.model.stats;

import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Created by sr186054 on 8/17/16.
 */
public class StatsModel {

    private static final Logger log = LoggerFactory.getLogger(StatsModel.class);


    /**
     * Failure Event Stats have a Total event count of 0, but a processorFailed count of 1 They are used to update the count of Processors that failed. The count is 0 because the system already has a
     * record for the event via the original stats it sent through when processing the flow.
     */
    public static ProvenanceEventStats toFailureProvenanceEventStats(String feedName, ProvenanceEventRecordDTO event) {
        ActiveFlowFile rootFlowFile = event.getFlowFile().getRootFlowFile();
        ProvenanceEventStats stats = new ProvenanceEventStats(feedName);
        stats.setTotalCount(0L);
        stats.setEventId(event.getEventId());
        stats.setProcessorId(event.getComponentId());
        stats.setProcessorName(event.getComponentName());
        stats.setClusterNodeId(event.getClusterNodeId());
        stats.setTime(event.getEventTime());
        stats.setProcessorsFailed(1L);
        stats.setFlowFileId(event.getFlowFileUuid());
        stats.setRootFlowFileId(rootFlowFile != null ? rootFlowFile.getId() : null);
        stats.setEventDetails(event.getDetails());
        stats.setRootProcessGroupId((rootFlowFile != null && rootFlowFile.hasFeedInformationAssigned()) ? rootFlowFile.getFeedProcessGroupId() : null);
        return stats;
    }


    /**
     * Used when an event has
     * @param feedName
     * @param event
     * @return
     */
    public static ProvenanceEventStats newJobCompletionProvenanceEventStats(String feedName, ProvenanceEventRecordDTO event) {
        ProvenanceEventStats stats = null;
        ActiveFlowFile rootFlowFile = event.getFlowFile().getRootFlowFile();
        if (!rootFlowFile.isFlowFileCompletionStatsCollected() && (event.isEndingFlowFileEvent() && rootFlowFile != null && rootFlowFile.isFlowComplete())) {
            rootFlowFile.setFlowFileCompletionStatsCollected(true);
            stats = new ProvenanceEventStats(feedName);
            stats.setTotalCount(0L);
            stats.setEventId(event.getEventId());
            stats.setProcessorId(event.getComponentId());
            stats.setProcessorName(event.getComponentName());
            stats.setClusterNodeId(event.getClusterNodeId());
            stats.setTime(event.getEventTime());
            stats.setFlowFileId(event.getFlowFileUuid());
            stats.setRootFlowFileId(rootFlowFile != null ? rootFlowFile.getId() : null);
            stats.setRootProcessGroupId((rootFlowFile != null && rootFlowFile.hasFeedInformationAssigned()) ? rootFlowFile.getFeedProcessGroupId() : null);
            stats.setJobsFinished(1L);
            event.setIsEndOfJob(true);
            log.info("New Completion job stats finished? {} for root: {} eventType: {} ", stats.getJobsFinished(), rootFlowFile.getId(), event.getEventType());
        }
        return stats;
    }


    /**
     * Convert the Event into a Stats model
     */
    public static ProvenanceEventStats toProvenanceEventStats(String feedName, ProvenanceEventRecordDTO event) {
        ActiveFlowFile rootFlowFile = event.getFlowFile().getRootFlowFile();

        ProvenanceEventStats stats = new ProvenanceEventStats(feedName);
        stats.setTotalCount(1L);
        stats.setEventId(event.getEventId());
        stats.setProcessorId(event.getComponentId());
        stats.setProcessorName(event.getComponentName());
        stats.setClusterNodeId(event.getClusterNodeId());
        stats.setTime(event.getEventTime());
        stats.setDuration(event.getEventDuration() != null ? event.getEventDuration() : 0L);
        stats.setBytesIn(event.getInputContentClaimFileSizeBytes() != null ? event.getInputContentClaimFileSizeBytes() : 0L);
        stats.setBytesOut(event.getOutputContentClaimFileSizeBytes() != null ? event.getOutputContentClaimFileSizeBytes() : 0L);
        stats.setFlowFileId(event.getFlowFileUuid());
        stats.setRootFlowFileId(rootFlowFile != null ? rootFlowFile.getId() : null);
        stats.setEventDetails(event.getDetails());
        stats.setRootProcessGroupId((rootFlowFile != null && rootFlowFile.hasFeedInformationAssigned()) ? rootFlowFile.getFeedProcessGroupId() : null);
        stats.setJobsStarted((event.getFlowFile().isRootFlowFile() && rootFlowFile.getFirstEvent() != null && event.equals(rootFlowFile.getFirstEvent())) ? 1L : 0L);
        stats.setJobsFinished((event.isEndingFlowFileEvent() && rootFlowFile != null && rootFlowFile.isFlowComplete() && !rootFlowFile.isFlowFileCompletionStatsCollected()) ? 1L : 0L);
        stats.setFlowFilesStarted(event.isStartOfCurrentFlowFile() ? 1L : 0L);
        stats.setFlowFilesFinished(event.getFlowFile().isCurrentFlowFileComplete() ? 1L : 0L);
        if (event.isTerminatedByFailureRelationship()) {
            //mark the flow file as having a failed event.
            event.getFlowFile().addFailedEvent(event);
        }
        if(event.isEndingFlowFileEvent()){
            log.info("ending flow file {} attached to root{} ... did we finish? {} ", event.getFlowFile().getId(), rootFlowFile.getId(), stats.getJobsFinished());
        }

        if (stats.getJobsFinished() == 1L) {
            rootFlowFile.setFlowFileCompletionStatsCollected(true);
            log.info("Finishing Job for flowfile: {} with eventtype: {}.  ", event.getFlowFile(), event.getEventType());
            event.setIsEndOfJob(true);
            Long jobTime = null;
            ProvenanceEventRecordDTO firstEvent = rootFlowFile.getFirstEvent() != null ? rootFlowFile.getFirstEvent() : event.getFlowFile().getFirstEvent();
            if (firstEvent != null) {
                jobTime = event.getEventTime().getMillis() - firstEvent.getEventTime().getMillis();
                stats.setJobDuration(jobTime);
            }

            Set<ProvenanceEventRecordDTO> failedEvents = event.getFlowFile().getRootFlowFile().getFailedEvents(true);
            if (failedEvents != null && !failedEvents.isEmpty()) {
                stats.setJobsFailed(1L);
            } else {
                if (jobTime != null) {
                    stats.setSuccessfulJobDuration(jobTime);
                }
            }


        }

        return stats;
    }


}