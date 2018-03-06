package br.com.dextra;

import junit.framework.Assert;
import org.junit.Test;

public class RepositoryHookTest {

    @Test
    public void subStringTestSimpleBranchName(){
        RepositoryHook repositoryHook = new RepositoryHook();
        String branchName = repositoryHook.getBranchName("refs/branch/myBranch");
        Assert.assertEquals("myBranch", branchName);
    }

    @Test
    public void subStringTestComplexBranchName(){
        RepositoryHook repositoryHook = new RepositoryHook();
        String branchName = repositoryHook.getBranchName("refs/branch/myBranch/myuri");
        Assert.assertEquals("myBranch/myuri", branchName);
    }

    @Test
    public void subStringTestTrickyBranchName(){
        RepositoryHook repositoryHook = new RepositoryHook();
        String branchName = repositoryHook.getBranchName("refs/branch/refs/branch/myBranch/myuri");
        Assert.assertEquals("refs/branch/myBranch/myuri", branchName);
    }
}
