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

});