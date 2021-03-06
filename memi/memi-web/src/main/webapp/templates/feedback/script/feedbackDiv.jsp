<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!-- ALTERNATIVE CODE TO JQUERY FEEDBACK POPUP JUST FOR IE6-->
<div id="feedback_no_div">
<div class="feedbackform_tab_v"><a id="script_feedbackLink" href="<c:url value="${baseURL}/feedback"/>" title="Give your feedback"></a>
</div>
</div>
<!-- END ALTERNATIVE CODE -->

<div id="feedback_div">
    <div class="feedbackform_tab_v">
        <a id="script_feedbackLink" href="javascript:slideFeedbackForm()" title="Give your feedback"></a>
        <a id="noscript_feedbackLink" href="<c:url value="${baseURL}/feedback"/>" title="Give your feedback"></a>
    </div>


    <div class="cont_feedback_form">
        <form id="feedback_form" action="**/doFeedback">
            <!--<div class="feedbackform_tab_o"></div>-->
            <!--<h3>Send us your feedback</h3>-->
            <fieldset>
                <table class="result">
                    <tr>
                        <td>
                            <div class="close_wind">
                                <a href="javascript:slideFeedbackForm()" title="Close this window"></a>
                            </div>
                            <label for="feedbackForm_emailAddress" class="required"><spring:message
                                    code="email.label"/></label>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input id="feedbackForm_emailAddress" name="emailAddress" type="text"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <label for="feedbackForm_emailSubject" class="required"><spring:message
                                    code="feedbackForm.inputField.subject.label"/></label>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input id="feedbackForm_emailSubject" name="emailSubject" type="text"/>
                        </td>
                    </tr>
                    <tr>
                        <td><label for="feedbackForm_emailMessage" class="required"><spring:message
                                code="feedbackForm.inputField.message.label"/></label>
                            <br/><textarea id="feedbackForm_emailMessage" name="emailMessage"></textarea>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input type="submit" name="submit" value="Submit" class="main_button"/>
                            <span class="clear_but">| <a href="javascript:clearFeedbackForm()" title="Clear the form">Clear</a></span>
                        </td>
                    </tr>
                    <input type="hidden" name="leaveIt" id="leaveIt"/>
                    <tr>
                        <td>
                            <span class="required"></span>&nbsp;
                            <small>required</small>
                        </td>
                    </tr>
                </table>
            </fieldset>
        </form>
    </div>
</div>