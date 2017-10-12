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

public class MyRepositoryHook implements AsyncPostReceiveRepositoryHook, RepositorySettingsValidator {

    public void validate(@Nonnull Settings settings, @Nonnull SettingsValidationErrors settingsValidationErrors, @Nonnull Repository repository) {

    }

    /**
     * Connects to a configured URL to notify of all changes.
     */
    public void postReceive(@Nonnull RepositoryHookContext repositoryHookContext, @Nonnull Collection<RefChange> collection) {
        String url = repositoryHookContext.getSettings().getString("url");
        String apiToken = repositoryHookContext.getSettings().getString("api_token");

        SimpleRefChange simpleRefChange = (SimpleRefChange) (new ArrayList(collection)).get(0);

        boolean isUpdate = simpleRefChange.getType() == RefChangeType.UPDATE;

        if (url != null && isUpdate) {

            String branch = simpleRefChange.getRefId().split("/")[2]; // TODO

            try {
                HttpClient httpclient = HttpClients.createDefault();
                HttpPost httppost = new HttpPost(url);

                Gson gson = new Gson();
                String body = gson.toJson(new PostBody(branch, apiToken));

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
    }

    public class PostBody{
        @SerializedName("hook_info")
        public HookInfo hookInfo;

        @SerializedName("build_params")
        public BuildParams buildParams;

        @SerializedName("triggered_by")
        public String triggeredBy;

        public PostBody(String branch, String apiToken) {
            this.hookInfo = new HookInfo(apiToken);
            this.buildParams = new BuildParams(branch);
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

        public BuildParams(String branch) {
            this.branch = branch;
        }

        public String getBranch() {
            return branch;
        }
    }
}
