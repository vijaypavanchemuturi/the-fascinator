
(function($){

  $(function(){
    // Date inputs
    $("input.dateYMD, input.date").datepicker({dateFormat:"yy-mm-dd", changeMonth:true, changeYear:true, showButtonPanel:false});
    function datepickerOnClose(dateText, inst){
        var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
        var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
        if(!month)month=0;
        $(this).datepicker('setDate', new Date(year, month, 1));
        $(this).blur();
    }
    function datepickerBeforeShow(input, inst){
        inst = $("#" + inst.id);
        if(inst.hasClass("dateMY") || inst.hasClass("dateYM") || inst.hasClass("dateY")){
          setTimeout(function(){
              $(".ui-datepicker-calendar").remove();
              $(".ui-datepicker-current").remove();
              $(".ui-datepicker-close").text("OK");
              if(inst.hasClass("dateY")) $(".ui-datepicker-month").remove();
            }, 10);
        }
    }
    $('input.dateYM').datepicker({
      changeMonth: true, changeYear: true, showButtonPanel: true, dateFormat: 'yy-mm',
      onClose: datepickerOnClose,
      beforeShow:datepickerBeforeShow,   
      onChangeMonthYear:function(year, month, inst){ datepickerBeforeShow(null, inst); },
      onSelect:function(dateText, inst){}
    });
    $('input.dateMMY').datepicker({
      changeMonth: true, changeYear: true, showButtonPanel: true, dateFormat: 'MM yy',
      onClose: datepickerOnClose,
      beforeShow:datepickerBeforeShow,   
      onChangeMonthYear:function(year, month, inst){ datepickerBeforeShow(null, inst); },
      onSelect:function(dateText, inst){}
    });
    $('input.dateY').datepicker({
      changeMonth: false, changeYear: true, showButtonPanel: true, dateFormat: 'yy',
      onClose: datepickerOnClose,
      beforeShow:datepickerBeforeShow,   
      onChangeMonthYear:function(year, month, inst){ datepickerBeforeShow(null, inst); },
      onSelect:function(dateText, inst){}
    });
    // ====
    
    // 
    alert("loaded ok ");
  });

})($);



