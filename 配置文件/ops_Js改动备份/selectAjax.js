function seachData(prefix){
	var dataUl = $("#dataList_hidden ul:eq(0)");
	$.ajax({
		type: "post",
		contentType: "application/x-www-form-urlencoded;charset=utf-8",
		url: __ctxPath+"/brandDisplay/queryBrandGroupListByName",
		dataType: "json",
		data: {
			"prefix" : prefix 
		},
		success: function(response) {
			dataUl.html("");
			if(response.success == "true"){
				var dataList = response.list;
				for(var i=0; i<dataList.length; i++){
					var li = "<li sid='" + dataList[i].sid + "' code='" + dataList[i].brandGroupCode + "'>"
						   + dataList[i].brandGroupName + "</li>";
					dataUl.append(li);
				}
				$("#dataList_hidden").show();
				liClick();
			}
		}
	});
}
function liClick(){
	$("#dataList_hidden ul li").click(function(){
		$("#BrandCode").html("<option value='"+$(this).attr("sid")+"' sid='"+$(this).attr("code")+"'>"+$(this).text()+"</option>");
		$("#BrandCode_input").val($(this).text());
		$("#dataList_hidden").hide();
		$("#BrandCode").change();
	});
}
$(function(){
	/*$("#BrandCode_input").blur(function(){
		if(!$("#dataList_hidden").focus()){
			$("#dataList_hidden").hide();
		}
	});*/
	$("#BrandCode_input").focus(function(){
		if($(this).val() != ""){
			seachData($(this).val())
		}
	});
});
