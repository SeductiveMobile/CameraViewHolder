package com.seductive.tools.cameraholder.async;

import android.os.AsyncTask;

public abstract class BaseAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected Exception exception;
    protected final static String SUCCESS = "success";

    @Override
    protected final Result doInBackground(Params... params) {
        try {
            return performInBackground(params);
        } catch (Exception e) {
            exception = e;
            return null;
        }
    }

    @Override
    protected final void onPostExecute(Result result) {
        if (exception == null) {
            onResult(result);
        } else {
            onException(exception);
        }
    }

    public abstract void onResult(Result result);

    public abstract void onException(Exception e);

    public abstract Result performInBackground(Params... params) throws Exception;
}