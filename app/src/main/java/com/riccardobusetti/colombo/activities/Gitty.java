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
        //setGuestOAuth2Token("b027dcedd6afe6a1487cead8518017a9ce82dcfa");


        // OPTIONAL METHODS

        // Set if User can send bugs with his own GitHub account (default: true)
        // If false, Gitty will always use your Auth token
        enableUserGitHubLogin(true);

        // Set if Gitty can use your Auth token for users without a GitHub account (default: true)
        // If false, Gitty will redirect non registred users to github.com/join
        enableGuestGitHubLogin(false);

        // Include other relevant info in your bug report (like custom variables)
        //setExtraInfo("Example string");

        // Allow users to edit debug info (default: false)
        canEditDebugInfo(true);

        // Customize Gitty appearance
        setFabColor1(Color.parseColor("#FFEB3B"), Color.parseColor("#FBC02D"), Color.parseColor("#FFD600"));
        setFabColor2(Color.parseColor("#E91E63"), Color.parseColor("#C2185B"), Color.parseColor("#C51162"));
        setBackgroundColor1(Color.parseColor("#673AB7"));
        setBackgroundColor2(Color.parseColor("#607D8B"));
    }
}
