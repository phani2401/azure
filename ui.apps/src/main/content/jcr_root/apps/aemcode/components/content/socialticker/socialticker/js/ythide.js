/**
 * Extension to the standard checkbox component. It enables/disables  other components based on the
 * selection made in the checkbox.
 *
 * How to use:
 *
 * - add the class cq-dialog-checkbox-enabledisable to the checkbox element
 * - add the class cq-dialog-checkbox-enabledisable-target to each target component that can be enabled/disabled
 */
$( document ).ready(function(){

 "use strict";
    function enableDisable(el){
		var target;
		target=el.data("cq-dialog-checkbox-enabledisable-target");
        el.each(function(i, element) {
            if ($(element).attr("type") === "checkbox"){
                if ($(element).prop('checked')){
                    $(target).removeClass("hide");
                } else {
                    $(target).addClass("hide");
                }
            }
        });
    }
    $.prototype.enable = function () {
        $.each(this, function (index, el) {
            $(this).removeAttr('disabled');
        });
    };
    $.prototype.disable = function () {
        $.each(this, function (index, el) {
            $(this).attr('disabled', 'disabled');
        });
    };
      // when dialog gets injected
    $(document).on("foundation-contentloaded", function(e) {
        // if there is already an inital value make sure the according target element becomes visible
        enableDisable($(".cq-dialog-checkbox-enabledisable-yt", e.target));
          });

    $(document).on("change", ".cq-dialog-checkbox-enabledisable-yt", function(e) {
        enableDisable($(this));
    });
});