$(function () {

    $(".phone__mask").mask("+7 (999) 999-99-99");
    
    var maxMsgLength = 70;
    if ($('#max_msg_length') !== null && $('#max_msg_length').html() !== undefined) {
        maxMsgLength = ($('#max_msg_length').html().trim());
        console.log("Max length set to " + maxMsgLength);
    }

    $('.js-tooltip').tooltip();

    $('.label_blue').click(function () {
        var dt = new Date();
        var time = dt.getHours() + ":" + dt.getMinutes() + ":" + dt.getSeconds();
        $('.textarea__message').val('Это тест в ' + time);
        var current = $('.textarea__message').val().length;
        var max = maxMsgLength.toString();
        var left = max - current;
        $('.counter').text(left);
    });

    $('.textarea__message').keyup(function () {
        var current = this.value.length;
        //var max = '335';
        current = current.toString();
        //max = max.toString();
        var left = maxMsgLength - current;
        $('.counter').text(left);
        if (this.value.length >= maxMsgLength) {
            this.value = this.value.substr(0, maxMsgLength);
            $('.counter').text('0');
        }
    });

    function inputCheckHighlight() {
        if ($('.auth_rtrn_checkValue').val() != 0) {
            $('.auth_rtrn_checkValue').removeClass('auth_rtrn_red');
        } else {
            $('.auth_rtrn_checkValue').addClass('auth_rtrn_red');
        }
    }

    inputCheckHighlight();

    $('.auth_rtrn_checkValue').keyup(function () {
        inputCheckHighlight();
    });

    function authRtrn() {
        $('.auth_rtrn').click(function () {
            if ($(this).is(':checked')) {
                $(this).siblings('.auth_rtrn_pass').hide();
            } else {
                $(this).siblings('.auth_rtrn_pass').show();
            }
        });
    }

    authRtrn();

    $('.auth_rtrn_pass').hide();

    $('.btn_hide-table').click(function () {
        var authRtrnShow = $(this).parent('.btn_hide-parent').siblings('.form_hide-assist').find('.auth_rtrn');
        if (authRtrnShow.is(':checked')) {
            authRtrnShow.siblings('.auth_rtrn_pass').hide();
        } else {
            authRtrnShow.siblings('.auth_rtrn_pass').show();
        }
        authRtrn();
    });

    $('.btn_add').click(function () {
        $('.btn_add_mech').toggleClass('inline-b');
    });

    $('.btn_hide').click(function () {
        $(this).toggleClass('btn_on');
        $('.form_hide').toggleClass('block');
    });

    $('.btn_hide-table').click(function () {
        $(this).toggleClass('btn_on-table');
        $(this).parent('.btn_hide-parent').siblings('.form_hide-assist').children('.form_hide-table').toggleClass('block');
        $(this).parent('.btn_hide-parent').siblings('.btn_hide-save').children('div').toggleClass('inline-b');
        $(this).parent('.btn_hide-parent').siblings('.form_hide-assist').children('.form_hide').toggleClass('none');
    });

    $('.publicKey_copy').click(function () {
        $(this).siblings('.publicKey_full').toggleClass('block');
    });

    $('#date_from').datepicker({
        onClose: function (selectedDate) {
            $('#date_to').datepicker("option", "minDate", selectedDate);
        }
    });

    $('#date_to').datepicker({
        onClose: function (selectedDate) {
            $('#date_from').datepicker("option", "maxDate", selectedDate);
        }
    });

    if ($('.input_date')[0]) {
        var dateMin = $('#date_from').val();
        var dateMax = $('#date_to').val()
        if (dateMin.length !== 0) {
            $('#date_to').datepicker("option", "minDate", 'dateMin');
        }
        if (dateMax.length !== 0) {
            $('#date_from').datepicker("option", "maxDate", 'dateMax');
        }
    }

    if ($('.textarea__key')[0]) {
        $('.label_wrong').hide();
        $('.textarea__key').keyup(function () {
            if (this.value.length > 256) {
                this.value = this.value.substr(0, 257);
                $('.label_wrong').show();
            } else {
                $('.label_wrong').show();
            }
            if (this.value.length == 256) {
                $('.label_wrong').hide();
            }
        });
    }

    function signalPoint() {
        var dBmValue = $('.counter_dBm').text();
        dBmValue = dBmValue.toString();
        if (dBmValue < -100) {
            $('.min').addClass('dBm_opacity');
            $('.low').addClass('dBm_opacity');
            $('.middle').addClass('dBm_opacity');
            $('.good').addClass('dBm_opacity');
        }
        if (dBmValue < -90 && dBmValue >= -100) {
            $('.min').removeClass('dBm_opacity');
            $('.low').addClass('dBm_opacity');
            $('.middle').addClass('dBm_opacity');
            $('.good').addClass('dBm_opacity');
        }
        if (dBmValue < -80 && dBmValue >= -90) {
            $('.min').removeClass('dBm_opacity');
            $('.low').removeClass('dBm_opacity');
            $('.middle').addClass('dBm_opacity');
            $('.good').addClass('dBm_opacity');
        }
        if (dBmValue < -70 && dBmValue >= -80) {
            $('.min').removeClass('dBm_opacity');
            $('.low').removeClass('dBm_opacity');
            $('.middle').removeClass('dBm_opacity');
            $('.good').addClass('dBm_opacity');
        }
        if (dBmValue >= -70) {
            $('.min').removeClass('dBm_opacity');
            $('.low').removeClass('dBm_opacity');
            $('.middle').removeClass('dBm_opacity');
            $('.good').removeClass('dBm_opacity');
        }
    }

    signalPoint();

    $('.counter_dBm').bind("DOMSubtreeModified", function () {
        signalPoint();
    });

    $('.private_key').hide();


    $('select').select2({
        dropdownCssClass: 'message__text',
        Infinity: -1
    });

    $('.icon-save').click(function () {
        var checkPass = $('#admin_pass').val().length;
        checkPass = checkPass.toString();
        console.log(checkPass);
        if (checkPass > 5) {
            $(this).children('.icon-save').prop('disabled', false);
        } else {
            $(this).children('.icon-save').prop('disabled', true);
            alert('Пароль должен состоять из минимум пяти символов!')
            return false;
        }
    });

});