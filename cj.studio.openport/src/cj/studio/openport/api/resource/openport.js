$(document).ready(function () {
    function syntaxHighlight(json) {
        if (typeof json !== 'string') {
            json = JSON.stringify(json, undefined, 4);
        }
        json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        return json.replace(
            /("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g,
            function (match) {
                let cls = 'number';
                if (/^"/.test(match)) {
                    if (/:$/.test(match)) {
                        cls = 'key'; // 匹配键
                    } else {
                        cls = 'string'; // 匹配字符串值
                    }
                } else if (/true|false/.test(match)) {
                    cls = 'boolean'; // 匹配布尔值
                } else if (/null/.test(match)) {
                    cls = 'null'; // 匹配 null
                }
                return '<span class="' + cls + '">' + match + '</span>';
            }
        );
    }

    function transformTime(timestamp) {
        if (timestamp) {
            var time = new Date(timestamp);
            var y = time.getFullYear(); //getFullYear方法以四位数字返回年份
            var M = time.getMonth() + 1; // getMonth方法从 Date 对象返回月份 (0 ~ 11)，返回结果需要手动加一
            var d = time.getDate(); // getDate方法从 Date 对象返回一个月中的某一天 (1 ~ 31)
            var h = time.getHours(); // getHours方法返回 Date 对象的小时 (0 ~ 23)
            var m = time.getMinutes(); // getMinutes方法返回 Date 对象的分钟 (0 ~ 59)
            var s = time.getSeconds(); // getSeconds方法返回 Date 对象的秒数 (0 ~ 59)
            return y + '-' + M + '-' + d + ' ' + h + ':' + m + ':' + s;
        } else {
            return '';
        }
    }

    $('.folder-main').on('click', function () {
        $(this).siblings('.pr-objs').toggle();
        $(this).find('.folder-arrow[direct=right]').toggle();
        $(this).find('.folder-arrow[direct=down]').toggle();
    });
    $('.obj-main').on('click', function () {
        $('.pr-folders .selected').removeClass('selected');
        $(this).addClass('selected');
        var porturl = $(this).parents('.pr-obj').attr('porturl');
        $('.portlet').removeClass('showing');
        $('.method-let[porturl=\"' + porturl + '\"]').addClass('showing');
        $('.res-bar li[runtimes] span').html('0');
        $('.res-bar li[taketime] span').html('-');
        $('.res-bar li[ctime] span').html('-');
        $('.res-bar li[state] span').html('-');
        $('.res-bar li[message] span').html('');
        $('.response > .content').empty();
    });
    $('.mask').on('click', function () {
        $(this).toggle();
    })
    $('.mask>.content').on('click', function (e) {
        e.stopPropagation();
        e.preventDefault();
        return false;
    })
    $('input[type=button][action=viewSimple]').on('click', function (e) {
        var url = $(this).attr('simplemodelfile');
        $.get(url, {}, function (json) {
            $('.mask>.content').html(json);
            $('.mask').toggle();
        }).error(function (e) {
            alert(e.responseText);
        });
    })
    $('input[type=button][action=run]').on('click', function (e) {
        var portlet = $('.portlet.method-let.showing');
        var params = portlet.find('.port-param');
        var appsignParams = portlet.find('.port-appsign-port-param');
        var reqcmd = portlet.attr('request-command');
        var porturl = portlet.attr('request-url');
        var portname = portlet.attr('portname');
        var tokenin = portlet.attr('tokenin');
        var checktokenname = portlet.attr('checktokenname');
        var tokenval = portlet.find('.request-token textarea').val();
        var headers = {'Rest-Command': portname};
        var parameters = {};
        var contents = {};
        switch (tokenin) {
            case "headersOfRequest":
                headers[checktokenname] = tokenval;
                break;
            case "parametersOfRequest":
                parameters[checktokenname] = tokenval;
                break;
        }
        for (var i = 0; i < appsignParams.length; i++) {
            var p = appsignParams[i];
            var paramName = $(p).attr('paramter-name');
            var paramValue = $(p).find('.port-appsign-argument').val();
            var inrequest = $(p).attr('inrequest');
            switch (inrequest) {
                case "header":
                    headers[paramName] = paramValue;
                    break;
                case "parameter":
                    parameters[paramName] = paramValue;
                    break;
            }
        }
        for (var i = 0; i < params.length; i++) {
            var p = params[i];
            var paramName = $(p).attr('paramter-name');
            var paramValue = $(p).find('.argument').val();
            var inrequest = $(p).attr('inrequest');
            switch (inrequest) {
                case "header":
                    headers[paramName] = paramValue;
                    break;
                case "parameter":
                    parameters[paramName] = paramValue;
                    break;
                case "content":
                    contents[paramName] = paramValue;
                    break;
            }

        }
        var reg = new RegExp("/$");
        if (!reg.test(porturl)) {
            porturl = porturl + "/";
        }


        var parseParam = function (param, key) {
            var paramStr = "";
            if (param instanceof String || param instanceof Number || param instanceof Boolean) {
                paramStr += "&" + key + "=" + encodeURIComponent(param);
            } else {
                $.each(param, function (i) {
                    var k = key == null ? i : key + (param instanceof Array ? "[" + i + "]" : "." + i);
                    paramStr += '&' + parseParam(this, k);
                });
            }
            return paramStr.substr(1);
        };

        var url = porturl + '?' + parseParam(parameters);


        function run(processTimes) {
            headers['OpenportsTester-Counter'] = processTimes;
            $.ajax({
                type: reqcmd,
                url: url,
                headers: headers,
                data: JSON.stringify(contents),//JSON.stringify(contents)
                contentType: "application/json",  //推荐写这个
                dataType: "json",
                success: function (obj) {
                    var taketime = obj.endtime - obj.begintime;
                    var begintime = transformTime(obj.begintime);
                    $('.res-bar li[taketime] span').html(taketime);
                    $('.res-bar li[ctime] span').html(begintime);
                    $('.res-bar li[state] span').html(obj.status);
                    $('.res-bar li[message] span').html(obj.message);
                    var contentE = $('.response .content');
                    contentE.empty();
                    var text = JSON.stringify(obj, null, 4);
                    contentE.append('<pre response_text>' + syntaxHighlight(text) + '</pre>');
                   var dataTextE= contentE.find('pre[response_text] .key').filter(function () {
                        return $(this).text().trim() === '"dataText":'; // 匹配内容为 "dataText":
                    });
                   var isToggle=false;
                    dataTextE.on('click',function (){
                        contentE.find('pre[dd_pre]').remove();
                        contentE.find('p').remove();
                        if(isToggle){
                            isToggle=false;
                            return;
                        }
                        var dataText=obj.dataText;
                        if(!dataText){
                            return;
                        }
                        var dataObj=JSON.parse(dataText);
                        var text = JSON.stringify(dataObj, null, 4);
                        contentE.append('<p class="data-text" style="margin-top: 10px;"><span style="font-size: 16px;font-weight: bold;">dataText:</span><pre dd_pre>'+syntaxHighlight(text)+'</pre></p>');
                        isToggle=true;
                    });
                    dataTextE.css('cursor', 'pointer');
                    dataTextE.css('textDecoration','underline');
                    dataTextE.prop('title','点击格式化');
                },
                error: function (e, textStatus, errorThrown) {
                    alert('错误:' + e.status + ' ' + e.responseText);
                }
            });
        };

        var timesE = portlet.find('.port-tools input[action=\"times\"]');
        var intervalE = portlet.find('.port-tools input[action=\"interval\"]');
        var timesText = timesE.val();
        var intervalText = intervalE.val();
        if (timesText == '' || parseInt(timesText) < 1) {
            timesText = "1";
            timesE.val(timesText);
        }
        if (intervalText == '' || parseInt(intervalText) < 0) {
            intervalText = "0";
            intervalE.val(intervalText);
        }
        var timesInt = parseInt(timesText);
        var ntervalInt = parseInt(intervalText);

        var processTimes = 0;
        var runtimesLable = $('.res-bar li[runtimes] span');
        if (timesInt == 1) {
            run(processTimes);
            processTimes++;
            runtimesLable.html(processTimes + '');
            return;
        }

        window.runnerButtonController = window.setInterval(function () {
            if (processTimes >= timesInt) {
                window.clearInterval(runnerButtonController);
                return;
            }
            run(processTimes);
            processTimes++;
            runtimesLable.html(processTimes + '');
        }, ntervalInt);

    });

    $('input[type=button][action=stop]').on('click', function (e) {
        window.clearInterval(runnerButtonController);
    });


});