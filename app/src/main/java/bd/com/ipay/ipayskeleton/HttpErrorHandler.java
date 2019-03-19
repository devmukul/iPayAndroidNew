package bd.com.ipay.ipayskeleton;

import android.app.Dialog;
import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.ViewModel.ProgressDialogListener;

public class HttpErrorHandler {

    public static boolean isErrorFound(GenericHttpResponse result, Context context, Dialog alertDialog) {
        if (alertDialog instanceof AnimatedProgressDialog) {
            if (result == null) {
                ((AnimatedProgressDialog) alertDialog).
                        showFailureAnimationAndMessage(context.getString(R.string.service_not_available));
                return true;
            } else if (result.getErrorMessage() != null) {
                ((AnimatedProgressDialog) alertDialog).showFailureAnimationAndMessage(result.getErrorMessage());
                return true;
            } else {
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
                    try {
                        GenericResponseWithMessageOnly genericResponseWithMessageOnly = new Gson().
                                fromJson(result.getJsonString(), GenericResponseWithMessageOnly.class);
                        ((AnimatedProgressDialog) alertDialog).showFailureAnimationAndMessage(genericResponseWithMessageOnly.getMessage());
                    } catch (Exception e) {
                        ((AnimatedProgressDialog) alertDialog).showFailureAnimationAndMessage("Not found");
                    }
                    return true;
                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR) {
                    ((AnimatedProgressDialog) alertDialog).showFailureAnimationAndMessage("Internal Server Error");
                    return true;
                }
                return false;
            }
        } else {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
            if (result == null) {
                return true;
            } else if (result.getErrorMessage() != null) {
                if (!result.isSilent()) {
                    Toast.makeText(context, result.getErrorMessage(), Toast.LENGTH_LONG).show();
                    return true;
                } else {
                    return true;
                }
            } else {
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
                    try {
                        GenericResponseWithMessageOnly genericResponseWithMessageOnly = new Gson().
                                fromJson(result.getJsonString(), GenericResponseWithMessageOnly.class);
                        if (!result.isSilent())
                            Toast.makeText(context, genericResponseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        Toast.makeText(context, "Not found", Toast.LENGTH_LONG).show();

                    }
                    return true;
                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR) {
                    if (!result.isSilent())
                        Toast.makeText(context, "Internal Server Error", Toast.LENGTH_LONG).show();
                    return true;
                }
                return false;
            }
        }
    }

    public static boolean isErrorFoundWithout404(GenericHttpResponse result, Context context, Dialog alertDialog) {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        if (result == null) {
            return true;
        } else if (result.getErrorMessage() != null) {
            if (!result.isSilent()) {
                Toast.makeText(context, result.getErrorMessage(), Toast.LENGTH_LONG).show();
            }
            return true;
        } else {
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR) {
                if (!result.isSilent()) {
                    Toast.makeText(context, "Internal Server Error", Toast.LENGTH_LONG).show();
                }
                return true;
            } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
                return true;
            }
        }
        return false;
    }

    public static boolean isErrorFoundForProgressDialogListener(GenericHttpResponse result, Context context, ProgressDialogListener alertDialog) {
        if (alertDialog != null) {
            if (result == null) {
                alertDialog.showFailureAnimationAndMessage(context.getString(R.string.service_not_available));
                return true;
            } else if (result.getErrorMessage() != null) {
                alertDialog.showFailureAnimationAndMessage(result.getErrorMessage());
                return true;
            } else {
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
                    try {
                        GenericResponseWithMessageOnly genericResponseWithMessageOnly = new Gson().
                                fromJson(result.getJsonString(), GenericResponseWithMessageOnly.class);
                        alertDialog.showFailureAnimationAndMessage(genericResponseWithMessageOnly.getMessage());
                    } catch (Exception e) {
                        alertDialog.showFailureAnimationAndMessage("Not found");
                    }
                    return true;
                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR) {
                    alertDialog.showFailureAnimationAndMessage("Internal Server Error");
                    return true;
                }
                return false;
            }
        } else {
            if (result == null) {
                return true;
            } else if (result.getErrorMessage() != null) {
                if (!result.isSilent()) {
                    Toast.makeText(context, result.getErrorMessage(), Toast.LENGTH_LONG).show();
                    return true;
                } else {
                    return true;
                }
            } else {
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
                    try {
                        GenericResponseWithMessageOnly genericResponseWithMessageOnly = new Gson().
                                fromJson(result.getJsonString(), GenericResponseWithMessageOnly.class);
                        if (!result.isSilent())
                            Toast.makeText(context, genericResponseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        if (!result.isSilent())
                            Toast.makeText(context, "Not found", Toast.LENGTH_LONG).show();

                    }
                    return true;
                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR) {
                    if (!result.isSilent())
                        Toast.makeText(context, "Internal Server Error", Toast.LENGTH_LONG).show();
                    return true;
                }
                return false;
            }
        }
    }

    public static boolean isErrorFound(GenericHttpResponse result, Context context) {
        if (result == null) {
            return true;
        } else if (result.getErrorMessage() != null) {
            if (!result.isSilent()) {
                Toast.makeText(context, result.getErrorMessage(), Toast.LENGTH_LONG).show();
                return true;
            } else {
                return true;
            }
        } else {
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
                try {
                    GenericResponseWithMessageOnly genericResponseWithMessageOnly = new Gson().
                            fromJson(result.getJsonString(), GenericResponseWithMessageOnly.class);
                    if (!result.isSilent())
                        Toast.makeText(context, genericResponseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    if (!result.isSilent())
                        Toast.makeText(context, "Not found", Toast.LENGTH_LONG).show();

                }
                return true;
            } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR) {
                if (!result.isSilent())
                    Toast.makeText(context, "Internal Server Error", Toast.LENGTH_LONG).show();
                return true;
            }
            return false;
        }
    }

}