$(document).ready(function(){

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
	$('.folder-main').on('click',function(){
		$(this).siblings('.pr-objs').toggle();
		$(this).find('.folder-arrow[direct=right]').toggle();
		$(this).find('.folder-arrow[direct=down]').toggle();
	});
	$('.obj-main').on('click',function(){
		$('.pr-folders .selected').removeClass('selected');
		$(this).addClass('selected');
		var porturl=$(this).parents('.pr-obj').attr('porturl');
		$('.portlet').removeClass('showing');
		$('.method-let[porturl=\"'+porturl+'\"]').addClass('showing');
	});
	$('.mask').on('click',function(){
		$(this).toggle();
	})
	$('.mask>.content').on('click',function(e){
		e.stopPropagation();
		e.preventDefault();
		return false;
	})
	$('input[type=button][action=viewSimple]').on('click',function(e){
		var v=$('simples>simple.returnvalue-simple').html();
		$('.mask>.content').html(v);
		$('.mask').toggle();
	})
	$('input[type=button][action=run]').on('click',function(e){
		var portlet=$('.portlet.method-let.showing');
		var params=portlet.find('.port-param');
		var reqcmd=portlet.attr('request-command');
		var porturl=portlet.attr('request-url');
		var portname=portlet.attr('portname');
		var tokenin=portlet.attr('tokenin');
		var checktokenname=portlet.attr('checktokenname');
		var tokenval=portlet.find('.request-token textarea').val();
		var headers={'Rest-Command':portname};
		var parameters={};
		var contents={};
		switch(tokenin){
		case "headersOfRequest":
            headers[checktokenname]=tokenval;
		break;
		case "parametersOfRequest":
		    parameters[checktokenname]=tokenval;
		break;
		}
		for(var i=0;i<params.length;i++){
		    var p=params[i];
		    var paramName=$(p).attr('paramter-name');
		    var paramValue=$(p).find('.argument').val();
		    var inrequest=$(p).attr('inrequest');
		    switch(inrequest){
		        case "header":
		        headers[paramName]=paramValue;
		        break;
		        case "parameter":
		        parameters[paramName]=paramValue;
		        break;
		        case "content":
                contents[paramName]=paramValue;
                break;
		    }

		}
		var   reg=new   RegExp("/$");
        if(!reg.test(porturl)){
            porturl=porturl+"/";
        }


        var parseParam=function(param, key){
        var paramStr="";
        if(param instanceof String||param instanceof Number||param instanceof Boolean){
        paramStr+="&"+key+"="+encodeURIComponent(param);
        }else{
        $.each(param,function(i){
        var k=key==null?i:key+(param instanceof Array?"["+i+"]":"."+i);
        paramStr+='&'+parseParam(this, k);
        });
        }
        return paramStr.substr(1);
        };

        var url=porturl+'?'+parseParam(parameters);


        function run(){
            $.ajax({
                         type: reqcmd,
                         url: url,
                         headers:headers,
                         data:JSON.stringify(contents),//JSON.stringify(contents)
                         contentType: "application/json",  //推荐写这个
                         dataType: "json",
                         success: function(obj){
                            var taketime=obj.endtime-obj.begintime;
                            var begintime=transformTime(obj.begintime);
                            $('.res-bar li[taketime] span').html(taketime);
                            $('.res-bar li[ctime] span').html(begintime);
                            $('.res-bar li[state] span').html(obj.status);
                            $('.res-bar li[message] span').html(obj.message);
                            var text=JSON.stringify(obj);
                         	$('.response .content').html(text);
                         },
                         error:function(e){
                             console.log(e.responseText);
                         }
                     });
        };

        var timesE=portlet.find('.port-tools input[action=\"times\"]');
        var intervalE=portlet.find('.port-tools input[action=\"interval\"]');
        var timesText=timesE.val();
        var intervalText=intervalE.val();
        if(timesText==''||parseInt(timesText)<1){
            timesText="1";
            timesE.val(timesText);
        }
        if(intervalText==''||parseInt(intervalText)<0){
            intervalText="0";
            intervalE.val(intervalText);
        }
        var timesInt=parseInt(timesText);
        var ntervalInt=parseInt(intervalText);
        if(timesInt==1){
            run();
            return;
        }
        var processTimes=0;
        var runtimesLable=$('.res-bar li[runtimes] span');
        window.runnerButtonController = window.setInterval(function() {
            if(processTimes>=timesInt){
                window.clearInterval(runnerButtonController);
                return;
            }
            run();
            runtimesLable.html(processTimes+'');
            processTimes++;
        },ntervalInt)

	});

	$('input[type=button][action=stop]').on('click',function(e){
       window.clearInterval(runnerButtonController);
	});
});