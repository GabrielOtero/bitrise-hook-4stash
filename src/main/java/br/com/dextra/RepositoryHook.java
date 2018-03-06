package br.com.dextra;

import com.atlassian.stash.hook.repository.AsyncPostReceiveRepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.RefChangeType;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.repository.SimpleRefChange;
import com.atlassian.stash.setting.RepositorySettingsValidator;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

public class RepositoryHook implements AsyncPostReceiveRepositoryHook, RepositorySettingsValidator {

    private static final String URL = "url";
    private static final String API_TOKEN = "api_token";
    private static final String PUSH_WORKFLOW_NAME = "push_workflow_name";
    private static final String TAG_WORKFLOW_NAME = "tag_workflow_name";

    public void validate(@Nonnull Settings settings, @Nonnull SettingsValidationErrors settingsValidationErrors, @Nonnull Repository repository) {

    }

    /**
     * Connects to a configured URL to notify of all changes.
     */
    public void postReceive(@Nonnull RepositoryHookContext repositoryHookContext, @Nonnull Collection<RefChange> collection) {
        String url = repositoryHookContext.getSettings().getString(URL);
        String apiToken = repositoryHookContext.getSettings().getString(API_TOKEN);

        String pushWorkflow = repositoryHookContext.getSettings().getString(PUSH_WORKFLOW_NAME);
        String tagWorkflow = repositoryHookContext.getSettings().getString(TAG_WORKFLOW_NAME);

        SimpleRefChange refChange = (SimpleRefChange) (new ArrayList(collection)).get(0);

        if (url != null) {
            String branchName = getBranchName(refChange.getRefId());

            if (isPush(refChange)) {
                onPush(url, apiToken, branchName, pushWorkflow);
            }

            if (isTag(refChange)) {
                onTag(url, apiToken, branchName, tagWorkflow);
            }
        }
    }

    String getBranchName(String refId) {
        int indexOf = refId.indexOf("/", refId.indexOf("/") + 1);
        return refId.substring(indexOf + 1, refId.length());
    }

    private void onTag(String url, String apiToken, String branch, String tagWorkflow) {
        try {
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost(url);

            Gson gson = new Gson();
            String body = gson.toJson(new PostBody(branch, apiToken, tagWorkflow));

            StringEntity stringEntity = new StringEntity(body);
            httppost.setEntity(stringEntity);

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                InputStream instream = entity.getContent();
                try {
                    // do something useful
                } finally {
                    instream.close();
                }
            }
        } catch (Exception e) {

        }
    }

    private void onPush(String url, String apiToken, String branch, String pushWorkflow) {
        try {
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost(url);

            Gson gson = new Gson();
            String body = gson.toJson(new PostBody(branch, apiToken, pushWorkflow));

            StringEntity stringEntity = new StringEntity(body);
            httppost.setEntity(stringEntity);

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                InputStream instream = entity.getContent();
                try {
                    // do something useful
                } finally {
                    instream.close();
                }
            }
        } catch (Exception e) {

        }
    }

    private boolean isTag(SimpleRefChange simpleRefChange) {
        return simpleRefChange.getType() == RefChangeType.ADD && simpleRefChange.getRefId().startsWith("refs/tags");
    }

    private boolean isPush(SimpleRefChange simpleRefChange) {
        return simpleRefChange.getType() == RefChangeType.UPDATE;
    }

    public class PostBody {
        @SerializedName("hook_info")
        public HookInfo hookInfo;

        @SerializedName("build_params")
        public BuildParams buildParams;

        @SerializedName("triggered_by")
        public String triggeredBy;

        public PostBody(String branch, String apiToken, String workflow) {
            this.hookInfo = new HookInfo(apiToken);
            this.buildParams = new BuildParams(branch, workflow);
            this.triggeredBy = "curl";
        }

        public HookInfo getHookInfo() {
            return hookInfo;
        }

        public BuildParams getBuildParams() {
            return buildParams;
        }

        public String getTriggeredBy() {
            return triggeredBy;
        }
    }

    private class HookInfo {
        public String type;

        @SerializedName("api_token")
        public String apiToken;

        public HookInfo(String apiToken) {
            this.type = "bitrise";
            this.apiToken = apiToken;
        }

        public String getType() {
            return type;
        }

        public String getApiToken() {
            return apiToken;
        }
    }

    private class BuildParams {
        public String branch;

        @SerializedName("workflow_id")
        public String workFlowId;

        public BuildParams(String branch, String workFlowId) {
            this.branch = branch;
            this.workFlowId = workFlowId;
        }

        public String getWorkFlowId() {
            return workFlowId;
        }

        public String getBranch() {
            return branch;
        }
    }
}
