// Copyright 2016 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////
package com.google.pubsub.clients;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

/**
 * A common interface for load test clients, to be implemented by protocol specific classes.
 */
public abstract class PubsubLoadClientAdapter {
  private final ThreadLocal<Integer> messageSequence;

  PubsubLoadClientAdapter() {
    messageSequence =
        new ThreadLocal<Integer>() {
          @Override
          protected Integer initialValue() {
            return 1;
          }
        };
  }

  public abstract ListenableFuture<RequestResult> createTopic(String topicName);

  public abstract ListenableFuture<RequestResult> createSubscription(
      String subscriptionName, String topicPath);

  public abstract ListenableFuture<PublishResponseResult> publishMessages(String topicPath);

  public abstract ListenableFuture<PullResponseResult> pullMessages(String subscriptionPath);

  protected int getNextMessageId(int increment) {
    int currentId = messageSequence.get();
    messageSequence.set(currentId + increment);
    return currentId;
  }

  /**
   * Encapsulates the information of the project and resource under load test.
   */
  public static class ProjectInfo {
    private final String project;
    private final String topic;
    private final String subscription;

    public ProjectInfo(String project, String topic, String subscription) {
      this.project = project;
      this.topic = topic;
      this.subscription = subscription;
    }

    public String getProject() {
      return project;
    }

    public String getTopic() {
      return topic;
    }

    public String getSubscription() {
      return subscription;
    }
  }

  /**
   * Encapsulates the parameters of the load test.
   */
  public static class LoadTestParams {
    private final String hostname;
    private final int publishBatchSize;
    private final int messageSize;
    private final int pullBatchSize;
    private final int concurrentPublishRequests;
    private final int concurrentPullRequests;
    private final int requestDeadlineMillis;

    public LoadTestParams(
        String hostname,
        int publishBatchSize,
        int messageSize,
        int pullBatchSize,
        int concurrentPublishRequests,
        int concurrentPullRequests,
        int requestDeadlineMillis) {
      this.hostname = hostname;
      this.publishBatchSize = publishBatchSize;
      this.messageSize = messageSize;
      this.pullBatchSize = pullBatchSize;
      this.concurrentPublishRequests = concurrentPublishRequests;
      this.concurrentPullRequests = concurrentPullRequests;
      this.requestDeadlineMillis = requestDeadlineMillis;
    }

    public String getHostname() {
      return hostname;
    }

    public int getPublishBatchSize() {
      return publishBatchSize;
    }

    public int getMessageSize() {
      return messageSize;
    }

    public int getPullBatchSize() {
      return pullBatchSize;
    }

    public int getConcurrentPublishRequests() {
      return concurrentPublishRequests;
    }

    public int getConcurrentPullRequests() {
      return concurrentPullRequests;
    }

    public int getRequestDeadlineMillis() {
      return requestDeadlineMillis;
    }
  }

  /**
   * Used to return the result of an request to the API.
   */
  public static class RequestResult {
    private final boolean ok;
    private final int statusCode;

    protected RequestResult(boolean ok, int statusCode) {
      this.ok = ok;
      this.statusCode = statusCode;
    }

    public boolean isOk() {
      return ok;
    }

    public int getStatusCode() {
      return statusCode;
    }
  }

  /**
   * A {@link RequestResult} specific to Publish operations.
   */
  public static final class PublishResponseResult extends RequestResult {
    private final int messagesPublished;

    protected PublishResponseResult(boolean ok, int statusCode, int messagesPublished) {
      super(ok, statusCode);
      this.messagesPublished = messagesPublished;
    }

    public int getMessagesPublished() {
      return messagesPublished;
    }
  }

  /**
   * A {@link RequestResult} specific to Pull operations.
   */
  public static final class PullResponseResult extends RequestResult {
    private final int messagesPulled;
    private List<Long> endToEndLatenciesMillis;

    protected PullResponseResult(
        boolean ok, int statusCode, int messagesProccessed, List<Long> endToEndLatenciesMillis) {
      super(ok, statusCode);
      this.messagesPulled = messagesProccessed;
      this.endToEndLatenciesMillis = endToEndLatenciesMillis;
    }

    public int getMessagesPulled() {
      return messagesPulled;
    }

    public List<Long> getEndToEndLatenciesMillis() {
      return endToEndLatenciesMillis;
    }
  }
}
