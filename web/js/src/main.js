$(function () {

    $('.label_blue').click(function () {
        $('.textarea__message').val('Это тест');
        var current = $('.textarea__message').val().length;
        var max = '70';
        max = max.toString();
        var left = max - current;
        $('.counter').text(left);
    });

    $('.textarea__message').keyup(function () {
        var current = this.value.length;
        var max = '70';
        current = current.toString();
        max = max.toString();
        var left = max - current;
        $('.counter').text(left);
        if (this.value.length >= 70) {
            this.value = this.value.substr(0, 70);
            $('.counter').text('0');
        }
    });

    $('.btn_hide').click(function () {
        $(this).toggleClass('btn_on');
        $('.form_hide').toggleClass('block');
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
                $('.label_wrong').hide();
            }
        });
    }

});