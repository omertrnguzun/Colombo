package com.riccardobusetti.colombo.activities;

import android.graphics.Color;
import android.os.Bundle;

import com.github.paolorotolo.gitty_reporter.GittyReporter;

/**
 * Created by riccardobusetti on 06/07/16.
 */

public class Gitty extends GittyReporter {

    // Please DO NOT override onCreate. Use init instead.
    @Override
    public void init(Bundle savedInstanceState) {

        // Set where Gitty will send issues.
        // (username, repository name);
        setTargetRepository("riccardobusetti", "Colombo");

        // Set Auth token to open issues if user doesn't have a GitHub account
        // For example, you can register a bot account on GitHub that will open bugs for you.
        setGuestOAuth2Token("8f53db1feb75a2e585c3fed4afe80b156f88ae1c");


        // OPTIONAL METHODS

        // Set if User can send bugs with his own GitHub account (default: true)
        // If false, Gitty will always use your Auth token
        enableUserGitHubLogin(true);

        // Set if Gitty can use your Auth token for users without a GitHub account (default: true)
        // If false, Gitty will redirect non registred users to github.com/join
        enableGuestGitHubLogin(true);

        // Include other relevant info in your bug report (like custom variables)
        setExtraInfo("Example string");

        // Allow users to edit debug info (default: false)
        canEditDebugInfo(true);

        // Customize Gitty appearance
        setFabColor1(Color.parseColor("#64FFDA"), Color.parseColor("#1DE9B6"), Color.parseColor("#1DE9B6"));
        setFabColor2(Color.parseColor("#64FFDA"), Color.parseColor("#1DE9B6"), Color.parseColor("#1DE9B6"));
        setBackgroundColor1(Color.parseColor("#80DEEA"));
        setBackgroundColor2(Color.parseColor("#80DEEA"));
        setRippleColor(Color.parseColor("#1DE9B6"));
    }
}
