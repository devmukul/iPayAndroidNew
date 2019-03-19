package bd.com.ipay.ipayskeleton.Aspect;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Arrays;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;

@Aspect
public class ServiceAccessValidatorAspect {

	@Around("execution(* on*Click(..)) && @annotation(bd.com.ipay.ipayskeleton.Aspect.ValidateAccess)")
	public Object serviceValidatorOnClick(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		ValidateAccess myAnnotation = method.getAnnotation(ValidateAccess.class);

		int[] serviceIds = myAnnotation.value();
		int[] orServiceIds = myAnnotation.or();
		if (serviceIds[0] == -1 && orServiceIds[0] != -1) {
			return validateOrAccess(joinPoint, orServiceIds);
		} else {
			return validateAndAccess(joinPoint, serviceIds);
		}
	}

	/**
	 * Checks if the user have the access of all the given service Ids.
	 */
	private Object validateAndAccess(ProceedingJoinPoint joinPoint, int[] serviceIds) throws Throwable {
		Object result = null;

		Logger.logW("ServiceIds", Arrays.toString(serviceIds));
		if (!ACLManager.hasServicesAccessibility(serviceIds)) {
			if (joinPoint.getArgs()[0] instanceof View) {
				View view = (View) joinPoint.getArgs()[0];
				DialogUtils.showServiceNotAllowedDialog(view.getContext());
			} else if (joinPoint.getArgs()[0] instanceof Dialog) {
				Dialog dialog = (Dialog) joinPoint.getArgs()[0];
				DialogUtils.showServiceNotAllowedDialog(dialog.getContext());
			}
		} else {
			result = joinPoint.proceed();
		}
		return result;
	}

	/**
	 * Checks if the user have the access of any one the given service Ids.
	 */
	private Object validateOrAccess(ProceedingJoinPoint joinPoint, int[] serviceIds) throws Throwable {
		Object result = null;

		Logger.logW("ServiceIds", Arrays.toString(serviceIds));
		if (!ACLManager.hasAnyOfServicesAccessibility(serviceIds)) {
			if (joinPoint.getArgs()[0] instanceof View) {
				View view = (View) joinPoint.getArgs()[0];
				DialogUtils.showServiceNotAllowedDialog(view.getContext());
			} else if (joinPoint.getArgs()[0] instanceof Dialog) {
				Dialog dialog = (Dialog) joinPoint.getArgs()[0];
				DialogUtils.showServiceNotAllowedDialog(dialog.getContext());
			}
		} else {
			result = joinPoint.proceed();
		}
		return result;
	}

	@Around("execution(* addContact(..)) && @annotation(bd.com.ipay.ipayskeleton.Aspect.ValidateAccess)")
	public Object serviceValidatorOnAddContact(ProceedingJoinPoint joinPoint) throws Throwable {

		Object result = null;

		if (ACLManager.hasServicesAccessibility(ServiceIdConstants.ADD_CONTACTS)) {
			result = joinPoint.proceed();
		}
		return result;
	}

	@Around("execution(* onCheckedChanged(..)) && @annotation(bd.com.ipay.ipayskeleton.Aspect.ValidateAccess)")
	public Object serviceValidatorOnRadioGroupCheckedChanged(ProceedingJoinPoint joinPoint) throws Throwable {

		Object result = null;
		RadioGroup radioGroup = (RadioGroup) joinPoint.getArgs()[0];
		final int checkedId = radioGroup.getCheckedRadioButtonId();

        Logger.logW("Aspect", checkedId + "");
        switch (radioGroup.getId()) {
            case R.id.transaction_history_type_radio_group:
                switch (checkedId) {
                    case R.id.pending_transaction_history_radio_button:
                        if (ACLManager.hasServicesAccessibility(ServiceIdConstants.PENDING_TRANSACTION)) {
                            result = joinPoint.proceed();
                        } else {
                            DialogUtils.showServiceNotAllowedDialog(radioGroup.getContext());
                        }
                        break;
                    case R.id.completed_transaction_history_radio_button:
                        if (ACLManager.hasServicesAccessibility(ServiceIdConstants.COMPLETED_TRANSACTION)) {
                            result = joinPoint.proceed();
                        } else {
                            DialogUtils.showServiceNotAllowedDialog(radioGroup.getContext());
                        }
                        break;
                }
                break;
            case R.id.contact_type_radio_group:
                if (ACLManager.hasServicesAccessibility(ServiceIdConstants.GET_CONTACTS)) {
                    result = joinPoint.proceed();
                } else {
                    DialogUtils.showServiceNotAllowedDialog(radioGroup.getContext());
                }
                break;
            default:
                result = joinPoint.proceed();
                break;
        }
        return result;
    }

    @Around("execution(* onNavigationItemSelected(..)) && @annotation(bd.com.ipay.ipayskeleton.Aspect.ValidateAccess)")
    public Object serviceValidatorOnNavigationItemSelect(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        boolean hasNavigationAccess = true;
        MenuItem item = (MenuItem) joinPoint.getArgs()[0];
        switch (item.getItemId()) {
            case R.id.nav_ipay_source_of_fund:
                hasNavigationAccess = ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_BANK_ACCOUNTS);
                break;
            case R.id.nav_user_activity:
                hasNavigationAccess = ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_ACTIVITY);
                break;
            case R.id.nav_logout:
                hasNavigationAccess = ACLManager.hasServicesAccessibility(ServiceIdConstants.SIGN_OUT);
                break;
            case R.id.nav_account:
            case R.id.nav_security_settings:
            case R.id.nav_help:
            case R.id.nav_about:
                hasNavigationAccess = true;
                break;
        }

		if (!hasNavigationAccess) {
			Context context = null;
			if (joinPoint.getTarget() instanceof Context) {
				context = (Context) joinPoint.getTarget();
			} else if (joinPoint.getTarget() instanceof Fragment) {
				context = ((Fragment) joinPoint.getTarget()).getContext();
			} else if (joinPoint.getTarget() instanceof android.app.Fragment) {
				context = ((android.app.Fragment) joinPoint.getTarget()).getActivity();
			}
			if (context != null) {
				DialogUtils.showServiceNotAllowedDialog(context);
			}
		} else {
			result = joinPoint.proceed();
		}
		return result;
	}

	@Around("execution(* switchToFragment(..)) && @annotation(bd.com.ipay.ipayskeleton.Aspect.ValidateAccess)")
	public Object serviceValidatorOnSwitchFragment(ProceedingJoinPoint joinPoint) throws Throwable {
		Object result = null;
		boolean canSwitchFragment = true;
		final String serviceName = (String) joinPoint.getArgs()[0];
		switch (serviceName) {
			case Constants.VERIFY_BANK:
				canSwitchFragment = ACLManager.hasServicesAccessibility(ServiceIdConstants.MANAGE_BANK_ACCOUNTS);
				break;
			case Constants.ADD_BANK:
				canSwitchFragment = ACLManager.hasServicesAccessibility(ServiceIdConstants.MANAGE_BANK_ACCOUNTS);
				break;
			case ProfileCompletionPropertyConstants.VERIFY_BANK_OR_CARD:
				canSwitchFragment = ACLManager.hasServicesAccessibility(ServiceIdConstants.MANAGE_BANK_ACCOUNTS);
				break;
			case ProfileCompletionPropertyConstants.PARENT:
				canSwitchFragment = ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_PARENT);
				break;
			case ProfileCompletionPropertyConstants.BASIC_PROFILE:
				canSwitchFragment = ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_PROFILE);
				break;
			case ProfileCompletionPropertyConstants.BUSINESS_INFO:
				canSwitchFragment = ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_BUSINESS_INFO);
				break;
			case ProfileCompletionPropertyConstants.INTRODUCER:
				canSwitchFragment = ACLManager.hasServicesAccessibility(ServiceIdConstants.MANAGE_INTRODUCERS);
				break;
			case ProfileCompletionPropertyConstants.PERSONAL_ADDRESS:
				canSwitchFragment = ACLManager.hasServicesAccessibility(ServiceIdConstants.MANAGE_ADDRESS);
				break;
			case ProfileCompletionPropertyConstants.BUSINESS_ADDRESS:
				canSwitchFragment = ACLManager.hasServicesAccessibility(ServiceIdConstants.MANAGE_ADDRESS);
				break;
			case ProfileCompletionPropertyConstants.VERIFIED_EMAIL:
				canSwitchFragment = ACLManager.hasServicesAccessibility(ServiceIdConstants.MANAGE_EMAILS);
				break;
			case ProfileCompletionPropertyConstants.BUSINESS_DOCUMENTS:
				canSwitchFragment = ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_BUSINESS_DOCS);
				break;
			case ProfileCompletionPropertyConstants.VERIFICATION_DOCUMENT:
				canSwitchFragment = ACLManager.hasServicesAccessibility(ServiceIdConstants.MANAGE_IDENTIFICATION_DOCS);
				break;
			case ProfileCompletionPropertyConstants.PHOTOID:
				canSwitchFragment = ACLManager.hasServicesAccessibility(ServiceIdConstants.MANAGE_IDENTIFICATION_DOCS);
				break;
			case ProfileCompletionPropertyConstants.PROFILE_COMPLETENESS:
				canSwitchFragment = ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_PROFILE_COMPLETION);
				break;
			case Constants.PROFILE_PICTURE:
				canSwitchFragment = ACLManager.hasServicesAccessibility(ServiceIdConstants.MANAGE_PROFILE_PICTURE);
				break;
			case ProfileCompletionPropertyConstants.PROFILE_INFO:
				canSwitchFragment = true;
				break;

		}

		if (!canSwitchFragment) {
			Context context = null;
			if (joinPoint.getTarget() instanceof Context) {
				context = (Context) joinPoint.getTarget();
			} else if (joinPoint.getTarget() instanceof Fragment) {
				context = ((Fragment) joinPoint.getTarget()).getContext();
			} else if (joinPoint.getTarget() instanceof android.app.Fragment) {
				context = ((android.app.Fragment) joinPoint.getTarget()).getActivity();
			}
			if (context != null) {
				DialogUtils.showServiceNotAllowedDialog(context);
			}
		} else {
			result = joinPoint.proceed();
		}
		return result;
	}
}
