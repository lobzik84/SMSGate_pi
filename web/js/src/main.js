$(function () {
    var maxMsgLength = 70;
    if ($('#max_msg_length') !== null && $('#max_msg_length').html() !== undefined) {
        maxMsgLength = ($('#max_msg_length').html().trim());
        console.log("Max length set to " + maxMsgLength);
    }
        
    $('.js-tooltip').tooltip();

    $('.label_blue').click(function () {
        $('.textarea__message').val('В вагоне отец попросил чай. Пока рассаживались, он вызвал помощника и попросил пригласить, как он выразился, отставших. Вскоре за столом сидели Кириченко, Брежнев, Кириленко, Устинов');
        var current = $('.textarea__message').val().length;;
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

    $('.btn_hide').click(function () {
        $(this).toggleClass('btn_on');
        $('.form_hide').toggleClass('block');
    });

    $('.btn_hide-table').click(function () {
        $(this).toggleClass('btn_on-table');
        $(this).siblings('.form_hide-table').toggleClass('block');
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
});