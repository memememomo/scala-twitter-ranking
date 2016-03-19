/// <reference path="lib/jquery.d.ts" />
$(function() {
    $(function() {
        $("[name=since]").change(function() {
            $("[type=submit]").click();
        });
    })
});

