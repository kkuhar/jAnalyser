var stranicaId = null;
var data = null;
var portlet_session_id=null;
var url1=null;

function dateDiff(date1,date2)
{
	var diffTime1;
	var dateUlaz = new Date(date1);
	var dateIzlaz = new Date(date2);
	var milliSecUlaz = dateUlaz.getTime();
	var milliSecIzlaz = dateIzlaz.getTime();
	var diffDate = (milliSecIzlaz-milliSecUlaz)/1000;
	var diffDateHours = parseInt((diffDate/60)/60);
	var diffDateMinutes = (diffDate/60)%60;
	if(parseInt(diffDateMinutes)<=9)
		{
		diffDateMinutes = "0"+diffDateMinutes;
		}
	diffTime1 = diffDateHours+"."+diffDateMinutes;
	return diffTime1;
}

function chackInsertTime(date1,date2)
{
	var diffTime1;
	var dateUlaz = new Date(date1);
	var dateIzlaz = new Date(date2);
	var milliSecUlaz = dateUlaz.getTime();
	var milliSecIzlaz = dateIzlaz.getTime();
	var diffDate = (milliSecIzlaz-milliSecUlaz)/1000;
	var diffDateHours = parseInt((diffDate/60)/60);
	var diffDateMinutes = (diffDate/60)%60;
	var ret = null;
	if(milliSecIzlaz<milliSecUlaz)
		{
		ret = "false";
		}
	else
		{
		ret = "true";
		}
		return ret;
}


function sumDate(time1,time2)
{
	var t1 = time1.replace(".",":"), t2 = time2.replace(".",":");
	var m = (t1.substring(0,t1.indexOf(':'))-0) * 60 +
	        (t1.substring(t1.indexOf(':')+1,t1.length)-0) +
	        (t2.substring(0,t2.indexOf(':'))-0) * 60 +
	        (t2.substring(t2.indexOf(':')+1,t2.length)-0);
	var h = Math.floor(m / 60);
	return h + '.' + (m - (h * 60));
}

function twelveHoursWorkingTime(dateFull1,dateFull2,id,diffTime)
{
	var standardWorkTime = dateFull1.slice(0,dateFull1.indexOf(" "))+" 12:00:00";
	var hoursUlaz = dateFull1.slice(dateFull1.indexOf(" "));
	var hoursIzlaz = dateFull2.slice(dateFull2.indexOf(" "));
	if(parseInt(diffTime)>=12)
	{

		document.getElementById("vr04"+id).value = "07:30";
		document.getElementById("vr05"+id).value = "15:30";

		document.getElementById("vr06"+id).value = "12.00";
		var diffTimeDatePlus = dateFull1.slice(0,dateFull1.indexOf(" "))+" "+diffTime.slice(0,diffTime.indexOf("."))+":"+diffTime.slice(diffTime.indexOf(".")+1)+":00"
		var prisutIzvanRadVremena = dateDiff(standardWorkTime,diffTimeDatePlus);
		document.getElementById("vr13"+id).value = prisutIzvanRadVremena;
		document.getElementById("vr09"+id).value = "0.0";

	}
	else
	{
		document.getElementById("vr04"+id).value = hoursUlaz.slice(1,6);
		document.getElementById("vr05"+id).value = hoursIzlaz.slice(1,6);
		document.getElementById("vr06"+id).value = diffTime;
		var diffTimeDateMinus = dateFull1.slice(0,dateFull1.indexOf(" "))+" "+diffTime.slice(0,diffTime.indexOf("."))+":"+diffTime.slice(diffTime.indexOf(".")+1)+":00"
		var satiNenazocnosti = dateDiff(diffTimeDateMinus,standardWorkTime);
		document.getElementById("vr09"+id).value = satiNenazocnosti;
		document.getElementById("vr13"+id).value = "0.0";
	}
}

function eightHoursWorkingTime(dateFull1,dateFull2,id,sifraOJ)
{
	var vrijednostVr04 = null;
	var vrijednostVr05 = null;
	var vrijednostVr06 = null;
	var vrijednostVr09 = null;
	var vrijednostVr13 = null;
	var diffTimeDatePlus = null;
	var prisutIzvanRadVremena = null;
	var diffTimeDateMinus = null;
	var satiNenazocnosti = null;
	var hoursUlaz = null;
	var hoursIzlaz = null;
	var standardWorkTime = dateFull1.slice(0,dateFull1.indexOf(" "))+" 08:00:00";
	var startDayTime = null;
	var startDayTimeUlaz = null;
	var endDayTime = null;
	var endDayTimeIzlaz = null;
	if(sifraOJ.slice(0,1)=='V')
		{
		if(sifraOJ == 'V100'||userData.ugovorZaposlenika=='2')
			{
			startDayTime = new Date(dateFull1.slice(0,dateFull1.indexOf(" "))+" 07:30:00");
			startDayTimeUlaz = dateFull1.slice(0,dateFull1.indexOf(" "))+" 07:30:00";
			startDayTimeKliznoUlazEnd = new Date(dateFull1.slice(0,dateFull1.indexOf(" "))+" 08:30:00");
			endDayTimeKliznoIzlazStart = new Date(dateFull1.slice(0,dateFull1.indexOf(" "))+" 15:30:00");
			endDayTime = new Date(dateFull1.slice(0,dateFull1.indexOf(" "))+" 16:30:00");
			endDayTimeIzlaz = dateFull2.slice(0,dateFull2.indexOf(" "))+" 16:30:00";
			}
		else{
		startDayTime = new Date(dateFull1.slice(0,dateFull1.indexOf(" "))+" 07:00:00");
		startDayTimeUlaz = dateFull1.slice(0,dateFull1.indexOf(" "))+" 07:00:00";
		endDayTime = new Date(dateFull1.slice(0,dateFull1.indexOf(" "))+" 15:00:00");
		endDayTimeIzlaz = dateFull2.slice(0,dateFull2.indexOf(" "))+" 15:00:00";
		}
		}
	else if(sifraOJ.slice(0,1)=='0')
		{
			if(sifraOJ.slice(0,5)=='00002')
				{
				startDayTime = new Date(dateFull1.slice(0,dateFull1.indexOf(" "))+" 07:00:00");
				startDayTimeUlaz = dateFull1.slice(0,dateFull1.indexOf(" "))+" 07:00:00";
				endDayTime = new Date(dateFull1.slice(0,dateFull1.indexOf(" "))+" 15:00:00");
				endDayTimeIzlaz = dateFull2.slice(0,dateFull2.indexOf(" "))+" 15:00:00";
				}
			else
				{
				startDayTime = new Date(dateFull1.slice(0,dateFull1.indexOf(" "))+" 07:30:00");
				startDayTimeUlaz = dateFull1.slice(0,dateFull1.indexOf(" "))+" 07:30:00";
				startDayTimeKliznoUlazEnd = new Date(dateFull1.slice(0,dateFull1.indexOf(" "))+" 08:30:00");
				endDayTimeKliznoIzlazStart = new Date(dateFull1.slice(0,dateFull1.indexOf(" "))+" 15:30:00");
				endDayTime = new Date(dateFull1.slice(0,dateFull1.indexOf(" "))+" 16:30:00");
				endDayTimeIzlaz = dateFull2.slice(0,dateFull2.indexOf(" "))+" 16:30:00";
				}
		}
	else
		{
		startDayTime = new Date(dateFull1.slice(0,dateFull1.indexOf(" "))+" 07:30:00");
		startDayTimeUlaz = dateFull1.slice(0,dateFull1.indexOf(" "))+" 07:30:00";
		startDayTimeKliznoUlazEnd = new Date(dateFull1.slice(0,dateFull1.indexOf(" "))+" 08:30:00");
		endDayTimeKliznoIzlazStart = new Date(dateFull1.slice(0,dateFull1.indexOf(" "))+" 15:30:00");
		endDayTime = new Date(dateFull1.slice(0,dateFull1.indexOf(" "))+" 16:30:00");
		endDayTimeIzlaz = dateFull2.slice(0,dateFull2.indexOf(" "))+" 16:30:00";
		}

	var ulazCheck = new Date(dateFull1);
	var izlazCheck = new Date(dateFull2);
	diffTime = null;

	if(startDayTime.getTime()<= ulazCheck.getTime() && endDayTime.getTime()>=izlazCheck.getTime())
		{
			//alert("petlja1 - 7:00 manje ili jednako ulazu / 15:00 veće ili jednako izlazu");
			diffTime = dateDiff(dateFull1,dateFull2);
			document.getElementById("vr03"+id).value = dateDiff(dateFull1,dateFull2);
			hoursUlaz = dateFull1.slice(dateFull1.indexOf(" "));
			hoursIzlaz = dateFull2.slice(dateFull2.indexOf(" "));

			if(parseInt(diffTime)>=8)
				{

					if(sifraOJ.slice(0,1)=='V')
						{
						if(sifraOJ == 'V100'||userData.ugovorZaposlenika=='2')
							{
							vrijednostVr04 = "07:30";
							vrijednostVr05 = "15:30";
							}
						else{
							vrijednostVr04 = "07:00";
							vrijednostVr05 = "15:00";
						}
						}
					else if(sifraOJ.slice(0,1)=='0')
						{
							if(sifraOJ.slice(0,5)=='00002')
								{
								vrijednostVr04 = "07:00";
								vrijednostVr05 = "15:00";
								}
							else
								{
								vrijednostVr04 = "07:30";
								vrijednostVr05 = "15:30";
								}
						}
					else
						{
						//alert("Ulazak :"+hoursUlaz+" -- Izlazak :"+hoursIzlaz);
						if(ulazCheck.getTime()>=startDayTime.getTime()&& ulazCheck.getTime()<=startDayTimeKliznoUlazEnd.getTime())
							{
							vrijednostVr04 = hoursUlaz.slice(1,6);
							}
						if(izlazCheck.getTime()>=endDayTimeKliznoIzlazStart.getTime()&& izlazCheck.getTime()<= endDayTime.getTime())
							{
							vrijednostVr05 = hoursIzlaz.slice(1,6);
							}
						else{
						vrijednostVr04 = "07:30";
						vrijednostVr05 = "15:30";
						}
						}

					vrijednostVr06 = "8.00";
					diffTimeDatePlus = dateFull1.slice(0,dateFull1.indexOf(" "))+" "+diffTime.slice(0,diffTime.indexOf("."))+":"+diffTime.slice(diffTime.indexOf(".")+1)+":00"
					prisutIzvanRadVremena = dateDiff(standardWorkTime,diffTimeDatePlus);
					vrijednostVr13 = prisutIzvanRadVremena;
					vrijednostVr09 = "0.00";

				}
			else
				{
					vrijednostVr04 = hoursUlaz.slice(1,6);
					vrijednostVr05 = hoursIzlaz.slice(1,6);
					vrijednostVr06 = diffTime;
					diffTimeDateMinus = dateFull1.slice(0,dateFull1.indexOf(" "))+" "+diffTime.slice(0,diffTime.indexOf("."))+":"+diffTime.slice(diffTime.indexOf(".")+1)+":00"
					satiNenazocnosti = dateDiff(diffTimeDateMinus,standardWorkTime);
					vrijednostVr09 = satiNenazocnosti;
					vrijednostVr13 = "0.00";

				}
			}
		else if(startDayTime.getTime()>= ulazCheck.getTime() && endDayTime.getTime()>=izlazCheck.getTime())
			{
				//Ako je ulaz manji od 7:00
				//alert("perlja 2 - 7:00 veće ili jednako ulazu / 15:00 veće ili jednako izlazu");
				diffTime = dateDiff(startDayTimeUlaz,dateFull2);
				document.getElementById("vr03"+id).value = dateDiff(dateFull1,dateFull2);
				hoursUlaz = startDayTimeUlaz.slice(standardWorkTime.indexOf(" "));
				hoursIzlaz = dateFull2.slice(dateFull2.indexOf(" "));
				if(parseInt(diffTime)>=8)
					{
					//alert("uslo u petlj više ili jednako 8 h")
						if(sifraOJ.slice(0,1)=='V')
							{
							if(sifraOJ == 'V100'||userData.ugovorZaposlenika=='2')
								{
								vrijednostVr04 = "07:30";
								vrijednostVr05 = "15:30";
								}
							else{
							vrijednostVr04 = "07:00";
							vrijednostVr05 = "15:00";
							}
							}
						else if(sifraOJ.slice(0,1)=='0')
							{
								if(sifraOJ.slice(0,5)=='00002')
									{
									vrijednostVr04 = "07:00";
									vrijednostVr05 = "15:00";
									}
								else
									{

									vrijednostVr04 = "07:30";
									vrijednostVr05 = "15:30";
									}
							}
						else
							{
							vrijednostVr04 = "07:30";
							vrijednostVr05 = "15:30";
							}

						vrijednostVr06 = dateDiff(dateFull1.slice(0,dateFull1.indexOf(" "))+" "+vrijednostVr04,dateFull1.slice(0,dateFull1.indexOf(" "))+" "+vrijednostVr05);
						diffTimeDatePlus = startDayTimeUlaz.slice(0,startDayTimeUlaz.indexOf(" "))+" "+diffTime.slice(0,diffTime.indexOf("."))+":"+diffTime.slice(diffTime.indexOf(".")+1)+":00"
						prisutIzvanRadVremena = dateDiff(standardWorkTime,diffTimeDatePlus);
						var satiDoStartWorkTime = dateDiff(dateFull1,startDayTimeUlaz);
						vrijednostVr13 = sumDate(satiDoStartWorkTime,prisutIzvanRadVremena);
						vrijednostVr09 = "0.00";


					}
				else
					{
						if(sifraOJ.slice(0,1)=='V')
							{
							if(sifraOJ == 'V100'||userData.ugovorZaposlenika=='2')
								{
								vrijednostVr04 = "07:30";
								}
							else{
								vrijednostVr04 = "07:00";
							}
							}
						else if(sifraOJ.slice(0,1)=='0')
							{
								if(sifraOJ.slice(0,5)=='00002')
									{
										vrijednostVr04 = "07:00";
									}
								else
									{
										vrijednostVr04 = "07:30";
									}
							}
						else
							{
								vrijednostVr04 = "07:30";
							}
						vrijednostVr05 = hoursIzlaz.slice(1,6);
						vrijednostVr06 = dateDiff(dateFull1.slice(0,dateFull1.indexOf(" "))+" "+vrijednostVr04,dateFull1.slice(0,dateFull1.indexOf(" "))+" "+vrijednostVr05);
						diffTimeDateMinus = dateFull1.slice(0,dateFull1.indexOf(" "))+" "+vrijednostVr06.slice(0,vrijednostVr06.indexOf("."))+":"+vrijednostVr06.slice(vrijednostVr06.indexOf(".")+1)+":00"


						satiNenazocnosti = dateDiff(diffTimeDateMinus,standardWorkTime);
						vrijednostVr09 = satiNenazocnosti;
						vrijednostVr13 = dateDiff(dateFull1,startDayTimeUlaz);

					}
			}
		else if (startDayTime.getTime()<= ulazCheck.getTime() && endDayTime.getTime()<=izlazCheck.getTime())
		{
			//Ako je ulaz veci od 17:00
			//alert("perlja 3 - 7:00 manje ili jednako ulazu / 15:00 manje ili jednako izlazu")
			diffTime = dateDiff(dateFull1,endDayTimeIzlaz);
			document.getElementById("vr03"+id).value = dateDiff(dateFull1,dateFull2);
			hoursUlaz = dateFull1.slice(dateFull1.indexOf(" "));
			hoursIzlaz = dateFull2.slice(dateFull2.indexOf(" "));
			if(parseInt(diffTime)>=8)
				{
				//alert("uslo u petlj više ili jednako 8 h")
					if(sifraOJ.slice(0,1)=='V')
						{
						if(sifraOJ=='V100'||userData.ugovorZaposlenika=='2')
							{
							vrijednostVr04 = "07:30";
							vrijednostVr05 = "15:30";
							}
						else{
						vrijednostVr04 = "07:00";
						vrijednostVr05 = "15:00";
						}
						}
					else if(sifraOJ.slice(0,1)=='0')
						{
							if(sifraOJ.slice(0,5)=='00002')
								{
								vrijednostVr04 = "07:00";
								vrijednostVr05 = "15:00";
								}
							else
								{
								vrijednostVr04 = "07:30";
								vrijednostVr05 = "15:30";
								}
						}
					else
						{
						vrijednostVr04 = "07:30";
						vrijednostVr05 = "15:30";
						}

					vrijednostVr06 = dateDiff(dateFull1.slice(0,dateFull1.indexOf(" "))+" "+vrijednostVr04,dateFull1.slice(0,dateFull1.indexOf(" "))+" "+vrijednostVr05);
					diffTimeDatePlus = startDayTimeUlaz.slice(0,startDayTimeUlaz.indexOf(" "))+" "+diffTime.slice(0,diffTime.indexOf("."))+":"+diffTime.slice(diffTime.indexOf(".")+1)+":00"
					prisutIzvanRadVremena = dateDiff(standardWorkTime,diffTimeDatePlus);
					var satiOdEndWorkTime = dateDiff(endDayTimeIzlaz,dateFull2);
					vrijednostVr13 = sumDate(satiOdEndWorkTime,prisutIzvanRadVremena);
					vrijednostVr09 = "0.00";
					//alert(prisutIzvanRadVremena);

				}
			else
				{
					vrijednostVr04 = hoursUlaz.slice(1,6);
					if(sifraOJ.slice(0,1)=='V')
						{
						if(sifraOJ == 'V100'||userData.ugovorZaposlenika=='2')
							{
							vrijednostVr05 = "15:30";
							}
						else{
							vrijednostVr05 = "15:00";
						}
						}
					else if(sifraOJ.slice(0,1)=='0')
						{
							if(sifraOJ.slice(0,5)=='00002')
								{
									vrijednostVr05 = "15:00";
								}
							else
								{
									vrijednostVr05 = "15:30";
								}
						}
					else
						{
							vrijednostVr05 = "15:30";
						}

					vrijednostVr06 = dateDiff(dateFull1.slice(0,dateFull1.indexOf(" "))+" "+vrijednostVr04,dateFull1.slice(0,dateFull1.indexOf(" "))+" "+vrijednostVr05);
					diffTimeDateMinus = dateFull1.slice(0,dateFull1.indexOf(" "))+" "+vrijednostVr06.slice(0,vrijednostVr06.indexOf("."))+":"+vrijednostVr06.slice(vrijednostVr06.indexOf(".")+1)+":00"

					//alert(diffTimeDateMinus)
					satiNenazocnosti = dateDiff(diffTimeDateMinus,standardWorkTime);
					vrijednostVr09 = satiNenazocnosti;
					vrijednostVr13 = dateDiff(endDayTimeIzlaz,dateFull2);
				}
		}
		else
		{

			diffTime = dateDiff(startDayTimeUlaz,endDayTimeIzlaz);
			document.getElementById("vr03"+id).value = diffTime;

			if(sifraOJ.slice(0,1)=='V')
				{
				if(sifraOJ=='V100'||userData.ugovorZaposlenika=='2')
					{
					vrijednostVr04 = "07:30";
					vrijednostVr05 = "15:30";
					}
				else{
					vrijednostVr04 = "07:00";
					vrijednostVr05 = "15:00";
				}
				}
			else if(sifraOJ.slice(0,1)=='0')
				{
					if(sifraOJ.slice(0,5)=='00002')
						{
							vrijednostVr04 = "07:00";
							vrijednostVr05 = "15:00";
						}
					else
						{

							vrijednostVr04 = "07:30";
							vrijednostVr05 = "15:30";
						}
				}
			else
				{
					vrijednostVr04 = "07:30";
					vrijednostVr05 = "15:30";
				}

			vrijednostVr06 = dateDiff(dateFull1.slice(0,dateFull1.indexOf(" "))+" "+vrijednostVr04,dateFull1.slice(0,dateFull1.indexOf(" "))+" "+vrijednostVr05);
			trueWorkHoursTemp = dateDiff (dateFull1,dateFull2);
			trueWorkHours = dateFull1.slice(0,dateFull1.indexOf(" "))+" "+trueWorkHoursTemp.slice(0,trueWorkHoursTemp.indexOf("."))+":"+trueWorkHoursTemp.slice(trueWorkHoursTemp.indexOf(".")+1)+":00";
			vrijednostVr13 = dateDiff(standardWorkTime,trueWorkHours);
			vrijednostVr09 = "0.00";
		}

	if(vrijednostVr09 != "0.00")
	{
		document.getElementById("vr10"+id).removeAttribute("readonly")
	}

	document.getElementById("vr04"+id).value = vrijednostVr04;
	document.getElementById("vr05"+id).value = vrijednostVr05;
	document.getElementById("vr06"+id).value = vrijednostVr06;
	document.getElementById("vr09"+id).value = vrijednostVr09;
	document.getElementById("vr13"+id).value = vrijednostVr13;
	document.getElementById("vr10"+id).value = "0.00";
	document.getElementById("vr11"+id).value = "0.00";

}

function caldate(dateFull1,dateFull2,id) {


	var inTime = new Date(dateFull1);
	var outTime= new Date(dateFull2);
	if(chackInsertTime(dateFull1,dateFull2)== "true")
		{
		if(dateFull1.slice(dateFull1.indexOf(" ")+1)== "00:00:00" && dateFull2.slice(dateFull2.indexOf(" ")+1)== "00:00:00")
		{
			msgboxChoose("Upozorenje",id,"warning");
		}
	else{
		document.getElementById("vr12"+id).value = "0.00";
		document.getElementById("vr16"+id).value = "0.00";
		document.getElementById("vr17"+id).value = "0.00";
		document.getElementById("vr22"+id).value = "0.00";
		document.getElementById("vr23"+id).value = "0.00";
		document.getElementById("vr24"+id).value = "0.00";
		document.getElementById("vr26"+id).value = "0.00";
		document.getElementById("vr27"+id).value = "0.00";
		var diffTime = null;
		var hoursUlaz = dateFull1.slice(dateFull1.indexOf(" "));
		var hoursIzlaz = dateFull2.slice(dateFull2.indexOf(" "));
		var sifraOJ = userData.sifraOJ;

		if(sifraOJ == 'S300')
		{
			diffTime = dateDiff(dateFull1,dateFull2);
			document.getElementById("vr03"+id).value = diffTime;
			twelveHoursWorkingTime(dateFull1,dateFull2,id,diffTime)	;
		}
		else
		{
			diffTime = dateDiff(dateFull1,dateFull2);

			document.getElementById("vr03"+id).value = diffTime;
			eightHoursWorkingTime(dateFull1,dateFull2,id,sifraOJ);
		}
	}
		}
	else{

		msgboxWarning("Upozorenje",'Vrijednost u stupcu ulazak je veća od vrijednosti u stupcu izlazak',"warning");
		document.getElementById("vr03"+id).value = "0.00";
		document.getElementById("vr04"+id).value = "";
		document.getElementById("vr05"+id).value = "";
		document.getElementById("vr06"+id).value = "0.00";
		document.getElementById("vr07"+id).value = "0.00";
		document.getElementById("vr08"+id).value = "0.00";
		document.getElementById("vr09"+id).value = "0.00";
		document.getElementById("vr10"+id).value = "0.00";
		document.getElementById("vr11"+id).value = "0.00";
		document.getElementById("vr12"+id).value = "0.00";
		document.getElementById("vr13"+id).value = "0.00";
		document.getElementById("vr14"+id).value = "0.00";
		document.getElementById("vr15"+id).value = "0.00";
		document.getElementById("vr16"+id).value = "0.00";
		document.getElementById("vr17"+id).value = "0.00";
		document.getElementById("vr18"+id).value = "0.00";
		document.getElementById("vr19"+id).value = "0.00";
		document.getElementById("vr20"+id).value = document.getElementById("vr20"+id).value;
		document.getElementById("vr21"+id).value = "0.00";
		document.getElementById("vr22"+id).value = "0.00";
		document.getElementById("vr23"+id).value = "0.00";
		document.getElementById("vr24"+id).value = "0.00";
		document.getElementById("vr25"+id).value = "0.00";
		document.getElementById("vr26"+id).value = "0.00";
		document.getElementById("vr27"+id).value = "0.00";


	}
}

function caldateNeradniDan(dateFull1,dateFull2,id)
{



	var inTime = new Date(dateFull1);
	var outTime= new Date(dateFull2);
	if(chackInsertTime(dateFull1,dateFull2)== "true")
		{
		if(dateFull1.slice(dateFull1.indexOf(" ")+1)== "00:00:00" && dateFull2.slice(dateFull2.indexOf(" ")+1)== "00:00:00")
		{
			msgboxChoose("Upozorenje",id,"warning");
		}
	else{
		document.getElementById("vr12"+id).value = "0.00";
		document.getElementById("vr16"+id).value = "0.00";
		document.getElementById("vr17"+id).value = "0.00";
		//document.getElementById("vr18"+id).value = "0.00";
		document.getElementById("vr22"+id).value = "0.00";
		document.getElementById("vr23"+id).value = "0.00";
		document.getElementById("vr24"+id).value = "0.00";
		document.getElementById("vr26"+id).value = "0.00";
		document.getElementById("vr27"+id).value = "0.00";
		var diffTime = null

			diffTime = dateDiff(dateFull1,dateFull2);
			var pocetakR = dateFull1.slice(dateFull1.indexOf(" "))
			var krajR = dateFull2.slice(dateFull2.indexOf(" "))
			document.getElementById("vr03"+id).value = diffTime;
			document.getElementById("vr04"+id).value = pocetakR.slice(1,6)
			document.getElementById("vr05"+id).value = krajR.slice(1,6)
			document.getElementById("vr13"+id).value = diffTime;

	}
		}
	else{
		msgboxWarning("Upozorenje",'Vrijednost u stupcu ulazak je veća od vrijednosti u stupcu izlazak',"warning");
		document.getElementById("vr03"+id).value = "0.00";
		document.getElementById("vr04"+id).value = "";
		document.getElementById("vr05"+id).value = "";
		document.getElementById("vr06"+id).value = "0.00";
		document.getElementById("vr07"+id).value = "0.00";
		document.getElementById("vr08"+id).value = "0.00";
		document.getElementById("vr09"+id).value = "0.00";
		document.getElementById("vr10"+id).value = "0.00";
		document.getElementById("vr11"+id).value = "0.00";
		document.getElementById("vr12"+id).value = "0.00";
		document.getElementById("vr13"+id).value = "0.00";
		document.getElementById("vr14"+id).value = "0.00";
		document.getElementById("vr15"+id).value = "0.00";
		document.getElementById("vr16"+id).value = "0.00";
		document.getElementById("vr17"+id).value = "0.00";
		//document.getElementById("vr18"+id).value = "0.00";
		document.getElementById("vr19"+id).value = "0.00";
		document.getElementById("vr20"+id).value = document.getElementById("vr20"+id).value;
		document.getElementById("vr21"+id).value = "0.00";
		document.getElementById("vr22"+id).value = "0.00";
		document.getElementById("vr23"+id).value = "0.00";
		document.getElementById("vr24"+id).value = "0.00";
		document.getElementById("vr25"+id).value = "0.00";
		document.getElementById("vr26"+id).value = "0.00";
		document.getElementById("vr27"+id).value = "0.00";

	}

}

function zbrojiVrijeme(id)
{
	var vrijednostUlaz=null;
	var vijednostIzlaz=null;
	var idredak = id.slice(4);//odvajanje datuma od sifre retka
	var datumRedak = idredak.slice(0,2)+"/"+idredak.slice(2,4)+"/"+idredak.slice(4);// stavljanje datuma u format dd/mm/gg
	vrijednostUlaz = datumRedak+" "+$("input#vr01"+idredak).val().replace(",",":")+":00";
	vijednostIzlaz = datumRedak+" "+$("input#vr02"+idredak).val().replace(",",":")+":00";

	document.getElementById("vr14"+id.slice(4)).value="0.00";
	if($("input#vr01"+idredak).val()=="" || $("input#vr02"+idredak).val()!="")
		{
			if($("input#vr01"+idredak).val()=="")
				{
					vrijednostUlaz = datumRedak+" "+"00:00:00";
				}
			if($("input#vr02"+idredak).val()=="")
				{
					vijednostIzlaz = datumRedak+" "+"00:00:00";
				}

			if(document.getElementById(id).className != "tablicaIzmjenaHolidayWeekDay cssPodnaslovi")
				{
					caldate(vrijednostUlaz,vijednostIzlaz,idredak)
				}
			else{
				caldateNeradniDan(vrijednostUlaz,vijednostIzlaz,idredak)
				}
		}
}

function submit_odrzavanje_dropDown(elem)
{

	if(elem == 'orgJedinica')
	{
    document.nazivOJform.submit();
	}
	else if(elem == 'zaposleniciUOJ')
	{
    document.popisZapFormName.submit();
	}
	else{}
}

function setSifraOJCombo() {
	//alert("nnnn"+userData.sifraOJ)
    try{
        if(userData.sifraOJ != null){
            $("#sifraOJ").find("option").each(function(){
                if($(this).val() == userData.sifraOJ)
                    $(this).attr("selected", "true");
                $("#sifraOJ").attr('class','cssDropDownList');
            });
        }
    }
    catch (e) {
       // alert("Dogodila se sistemska greska. Molimo Vas zapamtite broj greske i kontaktirajte administratora aplikacije -- Broj greske : 2");
    }

}
function setBrojZaposlenikaCombo() {
	//alert("nnnn"+userData.ugovorZaposlenika)
    try{
        if(userData.brojZaposlenika != null){
            $("#zaposlenikList").find("option").each(function(){
                if($(this).val() == userData.brojZaposlenika+"/"+userData.ugovorZaposlenika)
                    $(this).attr("selected", "true");
                $("#zaposlenikList").attr('class','cssDropDownList');
            });
        }
    }
    catch (e) {
        //alert("Dogodila se sistemska greska. Molimo Vas zapamtite broj greske i kontaktirajte administratora aplikacije -- Broj greske : 3");
    }

}

function setPeridOdCombo() {
	//alert("nnnn "+userData.period_od)
	if(userData.period_od != null)
		{
		document.getElementById("date").value = userData.period_od;
		}

}
function setPeridDoCombo() {
	//alert("nnnn "+userData.period_do)
	if(userData.period_od != null)
		{
		document.getElementById("date1").value = userData.period_do;
		}

}

function izbrisiperiodOd()
{
	document.getElementById("date1").value = ""
}
function izmjena_djelatnik()
{
document.getElementById("command1").value = "referent_izmjena";
document.izmjenaFormName.submit();
//setPeridOdCombo()
}

function tisak_referent(comm)
{
	msgboxWarningPrint("Upozorenje",comm+'-Dani koji nisu potvrdom uneseni u bazu nece se ispisati u izvjescu ',"warning");
	//alert("nesto");


}

function ispis_djelatnik()
{
document.getElementById("command1").value = "referent_ispis";
document.izmjenaFormName.submit();
}
function potvrdi_djelatnik()
{
msgboxWarning1("",'Pokrenuta je akcija potvrde podataka iz evidencije radnog vremena za djelatnika : <br/>'+userData.brojZaposlenika+' - '+userData.imePrezime+'<br/> u vremenskom razdoblju '+userData.period_od+' - '+userData.period_do+'<br/><br/> Želite li nastaviti?',"warning");
	//alert("Poslje")
}
function potvrdi_djelatnik_dnevno()
{
msgboxWarning2("",'Pokrenuta je akcija potvrde podataka iz evidencije radnog vremena za organizacijsku jedinicu : '+userData.oj_izmjene_dnevno+'<br/> na datum '+userData.dan_izmjene_dnevno+'<br/><br/> Želite li nastaviti?',"warning");
}

function ajaxPotvrdaDnevno() {
	//alert("AJAX POTVRDA DNEVNO");
	var brojRedovaTabliceIspis = document.getElementById('tablicaIspisDnevno').getElementsByTagName('tr').length;
	var idReda = null;
	var idCelije = null;
	var vrijednostCelije = null;
	var vrijednostiRedakaDnevno = null;
	var vrijednostPrisutnosti = new Array();
	var brojacListeVrstPrisutnsoati = 0;
	var vrijednostTerenPripravnost_I = new Array();
	var brojacListeVrstTerenPripravnost_I = 0;
	var vrijednostTerenPripravnost_II = new Array();
	var brojacListeVrstTerenPripravnost_II = 0;
	var vrijednostTerenskePrisutnosti = new Array();
	var brojacListeVrstTerenskePrisutnsoati = 0;
	var jednakost_pp = new Array();
	var brojacListeJednakostiPP = 0;
	var jednakost_pp_I = new Array();
	var brojacListeJednakostiPP_I = 0;
	var jednakost_pp_II = new Array();
	var brojacListeJednakostiPP_II = 0;

	for(var i = 1; i<=brojRedovaTabliceIspis-1;i++)
	{
	var brojStupacaRedovaTabliceIspis = document.getElementById('tablicaIspisDnevno').getElementsByTagName('tr')[i].getElementsByTagName('td');
	//alert(brojStupacaRedovaTabliceIspis.length);
		for(j=0;j<brojStupacaRedovaTabliceIspis.length;j++)
		{
			if(j==0)
			{
				//alert(brojStupacaRedovaTabliceIspis[j].id);
				idreda = brojStupacaRedovaTabliceIspis[j].id;
			}
			else if(j==1)
			{
			//alert("nestoDrugo");
			}
			else
				{
				if(j<=3)
				{
				vrijednostCelije=brojStupacaRedovaTabliceIspis[j].getElementsByTagName('input')[0].value.replace(",",":");
				}
			else{
				vrijednostCelije=brojStupacaRedovaTabliceIspis[j].getElementsByTagName('input')[0].value.replace(",",".");
			}
			idCelije = brojStupacaRedovaTabliceIspis[j].getElementsByTagName('input')[0].id;
			vrijednostiRedakaDnevno = vrijednostiRedakaDnevno +(idCelije+"="+vrijednostCelije)+"[,]";
			//alert(idCelije.slice(5)+" = "+vrijednostCelije);
			if(idCelije.slice(0,5)== "dvr19")
				{
				if(vrijednostCelije != "0.00")
					{
					if(document.getElementById(idCelije).className != "tablicaIzmjenaHolidayWeekDay cssPodnaslovi" && vrijednostCelije != "16.00")
						{
						vrijednostPrisutnosti[brojacListeVrstPrisutnsoati] = "rd."+idCelije+"."+vrijednostCelije;
						//alert(document.getElementById(idCelije).className+" -- "+vrijednostCelije+" -- "+idCelije)
						brojacListeVrstPrisutnsoati = brojacListeVrstPrisutnsoati +1;
						}
					if(document.getElementById(idCelije).className == "tablicaIzmjenaHolidayWeekDay cssPodnaslovi" && vrijednostCelije != "24.00")
						{
						vrijednostPrisutnosti[brojacListeVrstPrisutnsoati] = "vd."+idCelije+"."+vrijednostCelije;
						//alert(document.getElementById(idCelije).className+" -- "+vrijednostCelije+" -- "+idCelije)
						brojacListeVrstPrisutnsoati = brojacListeVrstPrisutnsoati +1;
						}
					}
				//alert(document.getElementById(idCelije).className+" -- "+vrijednostCelije)
				}

			/*else if(idCelije.slice(0,5)== "dvr21")
				{
				if(vrijednostCelije != "0.00")
					{
					if(document.getElementById(idCelije).className != "tablicaIzmjenaHolidayWeekDay cssPodnaslovi" && vrijednostCelije != "16.00")
						{
						vrijednostTerenskePrisutnosti[brojacListeVrstTerenskePrisutnsoati] = "rd."+idCelije+"."+vrijednostCelije;
						//alert(document.getElementById(idCelije).className+" -- "+vrijednostCelije+" -- "+idCelije)
						brojacListeVrstTerenskePrisutnsoati = brojacListeVrstTerenskePrisutnsoati +1;
						}
					if(document.getElementById(idCelije).className == "tablicaIzmjenaHolidayWeekDay cssPodnaslovi" && vrijednostCelije != "24.00")
						{
						vrijednostTerenskePrisutnosti[brojacListeVrstTerenskePrisutnsoati] = "vd."+idCelije+"."+vrijednostCelije;
						//alert(document.getElementById(idCelije).className+" -- "+vrijednostCelije+" -- "+idCelije)
						brojacListeVrstTerenskePrisutnsoati = brojacListeVrstTerenskePrisutnsoati +1;
						}
					}
				//alert(document.getElementById(idCelije).className+" -- "+vrijednostCelije)
				}*/
			else if(idCelije.slice(0,5)== "dvr21")
			{

				if(vrijednostCelije != "0.00")
				{
					if(document.getElementById("dvr19"+idCelije.slice(5)).value.replace(",",".") != vrijednostCelije)
					{
					if(document.getElementById(idCelije).className != "tablicaIzmjenaHolidayWeekDay cssPodnaslovi" && vrijednostCelije != "16.00")
						{
						vrijednostTerenskePrisutnosti[brojacListeVrstTerenskePrisutnsoati] = "rd."+idCelije+"."+vrijednostCelije;
						//alert(document.getElementById(idCelije).className+" -- "+vrijednostCelije+" -- "+idCelije)
						brojacListeVrstTerenskePrisutnsoati = brojacListeVrstTerenskePrisutnsoati +1;
						}
					if(document.getElementById(idCelije).className == "tablicaIzmjenaHolidayWeekDay cssPodnaslovi" && vrijednostCelije != "24.00")
						{
						vrijednostTerenskePrisutnosti[brojacListeVrstTerenskePrisutnsoati] = "vd."+idCelije+"."+vrijednostCelije;
						//alert(document.getElementById(idCelije).className+" -- "+vrijednostCelije+" -- "+idCelije)
						brojacListeVrstTerenskePrisutnsoati = brojacListeVrstTerenskePrisutnsoati +1;
						}
					}
					else{
						jednakost_pp[brojacListeJednakostiPP]= idCelije.slice(5)+" - "+document.getElementById("imePr"+idCelije.slice(5)).innerText;
						brojacListeJednakostiPP=brojacListeJednakostiPP+1;
					}
				}

			}
			/*else if(idCelije.slice(0,5)== "dvr48")
			{

				if(vrijednostCelije != "0.00")
				{
					//alert(document.getElementById("imePr"+idCelije.slice(5)).innerText)
					if(document.getElementById("dvr19"+idCelije.slice(5)).value.replace(",",".") != vrijednostCelije)
					{
					if(document.getElementById(idCelije).className == "tablicaIzmjenaHolidayWeekDay cssPodnaslovi" && vrijednostCelije != "24.00")
					{
					vrijednostTerenPripravnost_II[brojacListeVrstTerenPripravnost_II] = "vd."+idCelije+"."+vrijednostCelije;
					//alert(document.getElementById(idCelije).className+" -- "+vrijednostCelije+" -- "+idCelije)
					brojacListeVrstTerenPripravnost_II = brojacListeVrstTerenPripravnost_II +1;
					//alert("IF - vr48")
					}
					else if(document.getElementById(idCelije).className != "tablicaIzmjenaHolidayWeekDay cssPodnaslovi")
					{
					vrijednostTerenPripravnost_II[brojacListeVrstTerenPripravnost_II] = "rd."+idCelije+"."+vrijednostCelije;
					//alert(document.getElementById(idCelije).className+" -- "+vrijednostCelije+" -- "+idCelije)
					brojacListeVrstTerenPripravnost_II = brojacListeVrstTerenPripravnost_II +1;
					}
					}
					else
					{
						jednakost_pp_II[brojacListeJednakostiPP_II]= idCelije.slice(5)+" - "+document.getElementById("imePr"+idCelije.slice(5)).innerText;
						brojacListeJednakostiPP_II=brojacListeJednakostiPP_II+1;
					}

				}
			}*/
				}


		}
		vrijednostiRedakaDnevno = vrijednostiRedakaDnevno + "[|]";
		//alert(vrijednostiRedakaDnevno);
	}
	//alert(parseInt(vrijednostPrisutnosti.length) +" -- "+ parseInt(vrijednostTerenskePrisutnosti.length)+" -- "+parseInt(jednakost_pp.length))
	if(parseInt(vrijednostPrisutnosti.length) + parseInt(vrijednostTerenskePrisutnosti.length)+parseInt(jednakost_pp.length)== 0)
		//msgboxWarning1("",'Pokrenuta je akcija potvrde podataka iz evidencije radnog vremena za djelatnika : <br/>'+userData.brojZaposlenika+' - '+userData.imePrezime+'<br/> u vremenskom razdoblju '+userData.period_od+' - '+userData.period_do+'<br/><br/> Želite li nastaviti?',"warning");
		{
		var dataRukovoditeljDnevno = "&zaposlenikPotvrdaDnevno="+ vrijednostiRedakaDnevno + "&portlet_session_id=" + portlet_session_id + "&command=" + "ajax_referent_potvrda_dnevno";

		var n = $.ajax({

		    type: "POST",
		    url: url1,
		    data: dataRukovoditeljDnevno,
		    dataType: "html",
		    async:false,
		    success: function(msg){


		    			    closemsgbox2('btn_msgClose',this.parentNode.parentNode);
		    }
		}).responseText;


		}
		else
			{
			var porukaZaKrivuPrisutnost = "";
			var porukaZaKrivuTerenskuPripravnost_I = "";
			var porukaZaKrivuTerenskuPripravnost_II = "";
			if(jednakost_pp.length!=0)
			{
				porukaZaKrivuPrisutnost = "<b><font color='red'>Sati prisutnosti i tehnolo�ka pripravnost I su jednaki za zaposlenika :</font></b> <br/><br/>"

				for(var k = 0;k<brojacListeJednakostiPP_I;k++)
				{
					porukaZaKrivuPrisutnost = porukaZaKrivuPrisutnost+" - <b>"+jednakost_pp[k]+"</b><br/>";
				}
			}
			else if(vrijednostPrisutnosti.length!=0)
			{
			porukaZaKrivuPrisutnost = "<b><font color='red'>Sati prisutnosti</font> nisu pravilno uneseni za dane :</b> <br/><br/>"
			for (var k = 0 ; k < brojacListeVrstPrisutnsoati;k++)
			{
			if(vrijednostPrisutnosti[k].slice(0,2)== "rd")
				{
				porukaZaKrivuPrisutnost = porukaZaKrivuPrisutnost+" radni dan - ("+userData.dan_izmjene_dnevno+")- vrijednost : <font color='red'>"+vrijednostPrisutnosti[k].slice(15)+"</font> - (<font color='green'>16.00</font>)<br/>"
				//alert("radni dan - "+vrijednostPrisutnosti[k].slice(7,9)+"."+vrijednostPrisutnosti[k].slice(9,11)+"."+vrijednostPrisutnosti[k].slice(11,15)+" - "+vrijednostPrisutnosti[k].slice(16));
				}
			else
				{
				porukaZaKrivuPrisutnost = porukaZaKrivuPrisutnost+"neradni dan - ("+userData.dan_izmjene_dnevno+")- vrijednost : <font color='red'>"+vrijednostPrisutnosti[k].slice(15)+"</font> - (<font color='green'>24.00</font>)<br/>"
				//alert("neradni dan - "+vrijednostPrisutnosti[k].slice(7,9)+"."+vrijednostPrisutnosti[k].slice(9,11)+"."+vrijednostPrisutnosti[k].slice(11,15)+" - "+vrijednostPrisutnosti[k].slice(16));
				}
			}
			}
			else if(vrijednostTerenskePrisutnosti.length!=0)
				{
				porukaZaKrivuPrisutnost = "<b><font color='red'>Sati prisutnosti</font> nisu pravilno uneseni za dane :</b> <br/><br/>"
					for (var k = 0 ; k < brojacListeVrstPrisutnsoati;k++)
					{
					if(vrijednostTerenskePrisutnosti[k].slice(0,2)== "rd")
						{
						porukaZaKrivuPrisutnost = porukaZaKrivuPrisutnost+" radni dan - ("+userData.dan_izmjene_dnevno+")- vrijednost : <font color='red'>"+vrijednostTerenskePrisutnosti[k].slice(15)+"</font> - (<font color='green'>16.00</font>)<br/>"
						//alert("radni dan - "+vrijednostPrisutnosti[k].slice(7,9)+"."+vrijednostPrisutnosti[k].slice(9,11)+"."+vrijednostPrisutnosti[k].slice(11,15)+" - "+vrijednostPrisutnosti[k].slice(16));
						}
					else
						{
						porukaZaKrivuPrisutnost = porukaZaKrivuPrisutnost+"neradni dan - ("+userData.dan_izmjene_dnevno+")- vrijednost : <font color='red'>"+vrijednostTerenskePrisutnosti[k].slice(15)+"</font> - (<font color='green'>24.00</font>)<br/>"
						//alert("neradni dan - "+vrijednostPrisutnosti[k].slice(7,9)+"."+vrijednostPrisutnosti[k].slice(9,11)+"."+vrijednostPrisutnosti[k].slice(11,15)+" - "+vrijednostPrisutnosti[k].slice(16));
						}
					}
					}

			}
	msgboxWarningPrisutnost("Upozorenje",porukaZaKrivuPrisutnost,"warning");
}

function nazad_djelatnik()
{
document.popisZapFormName.submit();
}

function ajaxPotvrda() {

	document.getElementById("command1").value = "referent_potvrda";

//	alert("test")

	var brojRedovaTabliceIspis = document.getElementById('tablicaIspis').getElementsByTagName('tr').length;
	//alert(brojRedovaTabliceIspis);
	var idReda = null;
	var idCelije = null;
	var vrijednostCelije = null;
	var vrijednostiRedaka = null;
	var vrijednostPrisutnosti = new Array();
	var brojacListeVrstPrisutnsoati = 0;
	var vrijednostTerenPripravnost_I = new Array();
	var brojacListeVrstTerenPripravnost_I = 0;
	var vrijednostTerenskePrisutnosti = new Array();
	var brojacListeVrstTerenskePrisutnsoati = 0;
	var vrijednostTerenPripravnost_II = new Array();
	var brojacListeVrstTerenPripravnost_II = 0;
	var jednakost_pp_I = new Array();
	var brojacListeJednakostiPP_I = 0;
	var jednakost_pp_II = new Array();
	var jednakost_pp = new Array();
	var brojacListeJednakostiPP = 0;
	var brojacListeJednakostiPP_II = 0;


	for(var i = 1; i< brojRedovaTabliceIspis-1;i++)
		{
		var brojStupacaRedovaTabliceIspis = document.getElementById('tablicaIspis').getElementsByTagName('tr')[i].getElementsByTagName('td');
		//alert(brojStupacaRedovaTabliceIspis.length);
			for(j=0;j<brojStupacaRedovaTabliceIspis.length;j++)
			{
				if(j==0)
				{
					//alert(brojStupacaRedovaTabliceIspis[j].id);
					idreda = brojStupacaRedovaTabliceIspis[j].id;
				}
				else if(j==1)
				{
				//alert("nestoDrugo");
				}
				else
					{
					if(j<=3)
						{
						vrijednostCelije=brojStupacaRedovaTabliceIspis[j].getElementsByTagName('input')[0].value.replace(",",":");
						}
					else{
						vrijednostCelije=brojStupacaRedovaTabliceIspis[j].getElementsByTagName('input')[0].value.replace(",",".");
					}

				//alert(j+" -- "+vrijednostCelije)
				idCelije = brojStupacaRedovaTabliceIspis[j].getElementsByTagName('input')[0].id;
				vrijednostiRedaka = vrijednostiRedaka +(idCelije+"="+vrijednostCelije)+"[,]";
				//alert(idCelije+"("+idCelije.slice(4,12)+" = "+vrijednostCelije);
				if(idCelije.slice(0,4)== "vr19")
					{
					if(vrijednostCelije != "0.00")
						{
						if(document.getElementById(idCelije).className != "tablicaIzmjenaHolidayWeekDay cssPodnaslovi" && vrijednostCelije != "16.00")
							{
							vrijednostPrisutnosti[brojacListeVrstPrisutnsoati] = "rd."+idCelije+"."+vrijednostCelije;
							//alert(document.getElementById(idCelije).className+" -- "+vrijednostCelije+" -- "+idCelije)
							brojacListeVrstPrisutnsoati = brojacListeVrstPrisutnsoati +1;
							}
						if(document.getElementById(idCelije).className == "tablicaIzmjenaHolidayWeekDay cssPodnaslovi" && vrijednostCelije != "24.00")
							{
							vrijednostPrisutnosti[brojacListeVrstPrisutnsoati] = "vd."+idCelije+"."+vrijednostCelije;
							//alert(document.getElementById(idCelije).className+" -- "+vrijednostCelije+" -- "+idCelije)
							brojacListeVrstPrisutnsoati = brojacListeVrstPrisutnsoati +1;
							}
						}
					//alert(document.getElementById(idCelije).className+" -- "+vrijednostCelije)
					}
				else if(idCelije.slice(0,4)== "vr21")
					{
					if(vrijednostCelije != "0.00")
						{
						if(document.getElementById("vr19"+idCelije.slice(4,12)).value.replace(",",".")!=vrijednostCelije)
							{
							if(document.getElementById(idCelije).className != "tablicaIzmjenaHolidayWeekDay cssPodnaslovi" && vrijednostCelije != "16.00")
							{
							vrijednostTerenskePrisutnosti[brojacListeVrstTerenskePrisutnsoati] = "rd."+idCelije+"."+vrijednostCelije;
							//alert("radni dan na 21"+document.getElementById(idCelije).className+" -- "+vrijednostCelije+" -- "+idCelije)
							brojacListeVrstTerenskePrisutnsoati = brojacListeVrstTerenskePrisutnsoati +1;
							}
						if(document.getElementById(idCelije).className == "tablicaIzmjenaHolidayWeekDay cssPodnaslovi" && vrijednostCelije != "24.00")
							{
							vrijednostTerenskePrisutnosti[brojacListeVrstTerenskePrisutnsoati] = "vd."+idCelije+"."+vrijednostCelije;
							//alert("vikend na 21 "+document.getElementById(idCelije).className+" -- "+vrijednostCelije+" -- "+idCelije)
							brojacListeVrstTerenskePrisutnsoati = brojacListeVrstTerenskePrisutnsoati +1;
							}
							}
						else{
							jednakost_pp[brojacListeJednakostiPP]= idCelije.slice(4,12);
							brojacListeJednakostiPP=brojacListeJednakostiPP+1;
						}
						}
					}
				/*else if(idCelije.slice(0,4)== "vr21")
				{
					if(vrijednostCelije != "0.00")
					{
						//alert(vrijednostCelije+" -- "+document.getElementById("vr19"+idCelije.slice(4,12)).value)
						if(document.getElementById("vr19"+idCelije.slice(4,12)).value.replace(",",".")!=vrijednostCelije)
							{

						if(document.getElementById(idCelije).className != "tablicaIzmjenaHolidayWeekDay cssPodnaslovi" && vrijednostCelije != "16.00")
						{
						vrijednostTerenPripravnost_I[brojacListeVrstTerenPripravnost_I] = "rd."+idCelije+"."+vrijednostCelije;
						//alert(document.getElementById(idCelije).className+" -- "+vrijednostCelije+" -- "+idCelije)
						brojacListeVrstTerenPripravnost_I = brojacListeVrstTerenPripravnost_I +1;
						//alert("Stavi rd");
						}
						else if(document.getElementById(idCelije).className == "tablicaIzmjenaHolidayWeekDay cssPodnaslovi")
							{
							vrijednostTerenPripravnost_I[brojacListeVrstTerenPripravnost_I] = "vd."+idCelije+"."+vrijednostCelije;
							//alert(document.getElementById(idCelije).className+" -- "+vrijednostCelije+" -- "+idCelije)
							brojacListeVrstTerenPripravnost_I = brojacListeVrstTerenPripravnost_I +1;
							//alert("Stavi vd");
							}
							}
						else{
							//alert("else")
							jednakost_pp_I[brojacListeJednakostiPP_I]= idCelije.slice(4,12);
							brojacListeJednakostiPP_I=brojacListeJednakostiPP_I+1;

						}
					}
				}
				else if(idCelije.slice(0,4)== "vr48")
				{
					if(vrijednostCelije != "0.00")
					{
						if(document.getElementById("vr19"+idCelije.slice(4,12)).value.replace(",",".")!=vrijednostCelije)
							{
						if(document.getElementById(idCelije).className == "tablicaIzmjenaHolidayWeekDay cssPodnaslovi" && vrijednostCelije != "24.00")
						{
						vrijednostTerenPripravnost_II[brojacListeVrstTerenPripravnost_II] = "vd."+idCelije+"."+vrijednostCelije;
						//alert(document.getElementById(idCelije).className+" -- "+vrijednostCelije+" -- "+idCelije)
						brojacListeVrstTerenPripravnost_II = brojacListeVrstTerenPripravnost_II +1;
						//alert("IF - vr48")
						}
						else if(document.getElementById(idCelije).className != "tablicaIzmjenaHolidayWeekDay cssPodnaslovi")
						{
						vrijednostTerenPripravnost_II[brojacListeVrstTerenPripravnost_II] = "rd."+idCelije+"."+vrijednostCelije;
						//alert(document.getElementById(idCelije).className+" -- "+vrijednostCelije+" -- "+idCelije)
						brojacListeVrstTerenPripravnost_II = brojacListeVrstTerenPripravnost_II +1;
						}
					}

					else{
						jednakost_pp_II[brojacListeJednakostiPP_II]= idCelije.slice(4,12);
						brojacListeJednakostiPP_II=brojacListeJednakostiPP_II+1;
					}
					}
					}*/
				}
					}



			vrijednostiRedaka = vrijednostiRedaka + "[|]";
			//alert(vrijednostiRedaka);

		}
	//alert(parseInt(vrijednostPrisutnosti.length)+" -- "+parseInt(vrijednostTerenskePrisutnosti.length)+" -- "+parseInt(brojacListeJednakostiPP)+"---"+parseInt(brojacListeJednakostiPP_I)+parseInt(brojacListeJednakostiPP_II))
	if(parseInt(vrijednostPrisutnosti.length) + parseInt(vrijednostTerenskePrisutnosti.length)+parseInt(brojacListeJednakostiPP)== 0)
	//msgboxWarning1("",'Pokrenuta je akcija potvrde podataka iz evidencije radnog vremena za djelatnika : <br/>'+userData.brojZaposlenika+' - '+userData.imePrezime+'<br/> u vremenskom razdoblju '+userData.period_od+' - '+userData.period_do+'<br/><br/> Želite li nastaviti?',"warning");
	{
		//alert("nnnnnn")
	var dataRukovoditelj = "&zaposlenikPotvrda="+ vrijednostiRedaka + "&portlet_session_id=" + portlet_session_id + "&command=" + "ajax_referent_potvrda";
		//alert("&zaposlenikPotvrda="+ vrijednostiRedaka + "&portlet_session_id=" + portlet_session_id + "&command=" + "ajax_referent_potvrda")
	var n = $.ajax({

	    type: "POST",
	    url: url1,
	    data: dataRukovoditelj,
	    dataType: "html",
	    async: false,
	    success: function(msg){
		document.testForm.submit();
		ispunjavanje_analitikeAjax(msg);//Common.js
		closemsgbox2('btn_msgClose',this.parentNode.parentNode);

	    }
	}).responseText;

	}
	else
		{
		var porukaZaKrivuPrisutnost = "";
		var porukaZaKrivuTerenskuPripravnost_I = "";
		var porukaZaKrivuTerenskuPripravnost_II = "";
		if(jednakost_pp.length!=0)
		{
			porukaZaKrivuPrisutnost = "<b><font color='red'>Sati prisutnosti i tehnološka pripravnost I su jednaki na dane :</font></b> <br/><br/>"

			for(var k = 0;k<brojacListeJednakostiPP_I;k++)
			{
				porukaZaKrivuPrisutnost = porukaZaKrivuPrisutnost+" - "+jednakost_pp[k].slice(0,2)+"."+jednakost_pp[k].slice(2,4)+"."+jednakost_pp[k].slice(4)+"<br/>";
			}
		}
		/*else if(jednakost_pp_II.length!=0)
		{
			porukaZaKrivuPrisutnost = "<b><font color='red'>Sati prisutnosti i tehnološka pripravnost II su jednaki na dane :</font></b> <br/><br/>"

			for(var k = 0;k<brojacListeJednakostiPP_II;k++)
			{
				porukaZaKrivuPrisutnost = porukaZaKrivuPrisutnost+" - "+jednakost_pp_II[k].slice(0,2)+"."+jednakost_pp_II[k].slice(2,4)+"."+jednakost_pp_II[k].slice(4)+"<br/>";
			}
		}*/
		else if(vrijednostPrisutnosti.length!=0)
		{
		porukaZaKrivuPrisutnost = "<b><font color='red'>Sati prisutnosti</font> nisu pravilno uneseni za dane :</b> <br/><br/>"
		for (var k = 0 ; k < brojacListeVrstPrisutnsoati;k++)
		{

		if(vrijednostPrisutnosti[k].slice(0,2)== "rd")
			{
			porukaZaKrivuPrisutnost = porukaZaKrivuPrisutnost+" radni dan -("+vrijednostPrisutnosti[k].slice(7,9)+"."+vrijednostPrisutnosti[k].slice(9,11)+"."+vrijednostPrisutnosti[k].slice(11,15)+")- vrijednost : <font color='red'>"+vrijednostPrisutnosti[k].slice(16)+"</font> - (<font color='green'>16.00</font>)<br/>"
			//alert("radni dan - "+vrijednostPrisutnosti[k].slice(7,9)+"."+vrijednostPrisutnosti[k].slice(9,11)+"."+vrijednostPrisutnosti[k].slice(11,15)+" - "+vrijednostPrisutnosti[k].slice(16));
			}
		else
			{
			porukaZaKrivuPrisutnost = porukaZaKrivuPrisutnost+"neradni dan -("+vrijednostPrisutnosti[k].slice(7,9)+"."+vrijednostPrisutnosti[k].slice(9,11)+"."+vrijednostPrisutnosti[k].slice(11,15)+")- vrijednost : <font color='red'>"+vrijednostPrisutnosti[k].slice(16)+"</font> - (<font color='green'>24.00</font>)<br/>"
			//alert("neradni dan - "+vrijednostPrisutnosti[k].slice(7,9)+"."+vrijednostPrisutnosti[k].slice(9,11)+"."+vrijednostPrisutnosti[k].slice(11,15)+" - "+vrijednostPrisutnosti[k].slice(16));
			}
		}
		}
		else if(vrijednostTerenskePrisutnosti.length!=0)
		{
		porukaZaKrivuPrisutnost = "<b><font color='red'>Sati tehnološke pripravnosti</font> nisu pravilno uneseni za dane :</b> <br/><br/>"

		for (var k = 0 ; k < brojacListeVrstTerenskePrisutnsoati;k++)
		{
		if(vrijednostTerenskePrisutnosti[k].slice(0,2)== "rd")
			{
			porukaZaKrivuPrisutnost = porukaZaKrivuPrisutnost+" radni dan -("+vrijednostTerenskePrisutnosti[k].slice(7,9)+"."+vrijednostTerenskePrisutnosti[k].slice(9,11)+"."+vrijednostTerenskePrisutnosti[k].slice(11,15)+")- vrijednost : <font color='red'>"+vrijednostTerenskePrisutnosti[k].slice(16)+"</font> - (<font color='green'>16.00</font>)<br/>"
			//alert("radni dan - "+vrijednostPrisutnosti[k].slice(7,9)+"."+vrijednostPrisutnosti[k].slice(9,11)+"."+vrijednostPrisutnosti[k].slice(11,15)+" - "+vrijednostPrisutnosti[k].slice(16));
			}
		else
			{
			porukaZaKrivuPrisutnost = porukaZaKrivuPrisutnost+"neradni dan -("+vrijednostTerenskePrisutnosti[k].slice(7,9)+"."+vrijednostTerenskePrisutnosti[k].slice(9,11)+"."+vrijednostTerenskePrisutnosti[k].slice(11,15)+")- vrijednost : <font color='red'>"+vrijednostTerenskePrisutnosti[k].slice(16)+"</font> - (<font color='green'>24.00</font>)<br/>"
			//alert("neradni dan - "+vrijednostPrisutnosti[k].slice(7,9)+"."+vrijednostPrisutnosti[k].slice(9,11)+"."+vrijednostPrisutnosti[k].slice(11,15)+" - "+vrijednostPrisutnosti[k].slice(16));
			}
		}
		}
		/*else if(vrijednostTerenPripravnost_I.length!=0)
			{
			porukaZaKrivuPrisutnost = "<b><font color='red'>Sati tehnološke pripravnosti I</font> nisu pravilno uneseni za dane :</b> <br/><br/>"
				for (var k = 0 ; k < brojacListeVrstTerenPripravnost_I;k++)
				{
				//alert(vrijednostTerenPripravnost_I[k].slice(0,2))
				if(vrijednostTerenPripravnost_I[k].slice(0,2)== "rd")
					{
					porukaZaKrivuPrisutnost = porukaZaKrivuPrisutnost+"radni dan -("+vrijednostTerenPripravnost_I[k].slice(7,9)+"."+vrijednostTerenPripravnost_I[k].slice(9,11)+"."+vrijednostTerenPripravnost_I[k].slice(11,15)+")- vrijednost : <font color='red'>"+vrijednostTerenPripravnost_I[k].slice(16)+"</font>- (<font color='green'>16.00</font>)<br/>"
					//alert("radni dan - "+vrijednostPrisutnosti[k].slice(7,9)+"."+vrijednostPrisutnosti[k].slice(9,11)+"."+vrijednostPrisutnosti[k].slice(11,15)+" - "+vrijednostPrisutnosti[k].slice(16));
					}
				else
					{
					porukaZaKrivuPrisutnost = porukaZaKrivuPrisutnost+"<font color='red'>neradni dan</font> -("+vrijednostTerenPripravnost_I[k].slice(7,9)+"."+vrijednostTerenPripravnost_I[k].slice(9,11)+"."+vrijednostTerenPripravnost_I[k].slice(11,15)+")- <font color='red'>nedozvoljeni unos</font><br/>"
					//alert("neradni dan - "+vrijednostPrisutnosti[k].slice(7,9)+"."+vrijednostPrisutnosti[k].slice(9,11)+"."+vrijednostPrisutnosti[k].slice(11,15)+" - "+vrijednostPrisutnosti[k].slice(16));
					}
				}
			}*/
		/*else if(vrijednostTerenPripravnost_II.length!=0)
		{
			porukaZaKrivuPrisutnost = "<b><font color='red'>Sati tehnološke pripravnosti II</font> nisu pravilno uneseni za dane :</b> <br/><br/>"
			for (var k = 0 ; k < brojacListeVrstTerenPripravnost_II;k++)
			{
			if(vrijednostTerenPripravnost_II[k].slice(0,2)== "rd")
				{
				porukaZaKrivuPrisutnost = porukaZaKrivuPrisutnost+"<font color='red'>radni dan</font> -("+vrijednostTerenPripravnost_II[k].slice(7,9)+"."+vrijednostTerenPripravnost_II[k].slice(9,11)+"."+vrijednostTerenPripravnost_II[k].slice(11,15)+")- <font color='red'>nedozvoljeni unos</font><br/>"
				//alert("radni dan - "+vrijednostPrisutnosti[k].slice(7,9)+"."+vrijednostPrisutnosti[k].slice(9,11)+"."+vrijednostPrisutnosti[k].slice(11,15)+" - "+vrijednostPrisutnosti[k].slice(16));
				}
			else
				{
				porukaZaKrivuPrisutnost = porukaZaKrivuPrisutnost+"neradni dan -("+vrijednostTerenPripravnost_II[k].slice(7,9)+"."+vrijednostTerenPripravnost_II[k].slice(9,11)+"."+vrijednostTerenPripravnost_II[k].slice(11,15)+")- vrijednost : <font color='red'>"+vrijednostTerenPripravnost_II[k].slice(16)+"</font>- (<font color='green'>24.00</font>)<br/>"
				//alert("neradni dan - "+vrijednostPrisutnosti[k].slice(7,9)+"."+vrijednostPrisutnosti[k].slice(9,11)+"."+vrijednostPrisutnosti[k].slice(11,15)+" - "+vrijednostPrisutnosti[k].slice(16));
				}
			}
		}*/
		}

		msgboxWarningPrisutnost("Upozorenje",porukaZaKrivuPrisutnost,"warning");

}

//document.izmjenaFormName.submit();



function potvrdiUnos_djelatnik()
{
	msgboxDnevniUnos("",userData.trans_dnevni_unos_msg,"warning");

}
function odbaciUnos_djelatnik()
{
	document.getElementById("pocetakRadaSati").value = "";
	document.getElementById("pocetakRadaMin").value = "";
	document.getElementById("zavrsetakRadaSati").value = "";
	document.getElementById("zavrsetakRadaMin").value = "";
}

function tisak_djelatnik()
{
	var dataTisak = "&zaposlenikTisak="+ "" + "&portlet_session_id=" + portlet_session_id + "&command=" + "ajax_referent_tisak";

	var n = $.ajax({

	    type: "POST",
	    url: url1,
	    data: dataTisak,
	    dataType: "html",
	    success: function(msg){
	        alert(msg);
	    }
	}).responseText;
}

function auto_djelatnik()
{
	var brojRedovaTabliceIspis = document.getElementById('tablicaIspis').getElementsByTagName('tr').length;
	var idReda = null;
	var idCelije = null;
	var vrijednostCelije = null;
	var vrijednostiRedaka = null;
	var vrijednostiPrvogRetka = new Array();


	var prviStupacaRedovaTabliceIspis = document.getElementById('tablicaIspis').getElementsByTagName('tr')[1].getElementsByTagName('td');
	//alert(prviStupacaRedovaTabliceIspis[3].getElementsByTagName('input')[0].className)
	if(prviStupacaRedovaTabliceIspis[3].getElementsByTagName('input')[0].className == "tablicaIzmjenaHolidayWeekDay cssPodnaslovi")
	{}
	else{
	for(j=0;j<prviStupacaRedovaTabliceIspis.length;j++)
		{
			if(j==0)
				{
				vrijednostiPrvogRetka [j] = "0";
				//alert(brojStupacaRedovaTabliceIspis[j].id);
				//idreda = brojStupacaRedovaTabliceIspis[j].id;
				}
			else if(j==1)
				{
				vrijednostiPrvogRetka [j] = "0";
				}
			else{
				vrijednostCelije=prviStupacaRedovaTabliceIspis[j].getElementsByTagName('input')[0].value;
				idCelije = prviStupacaRedovaTabliceIspis[j].getElementsByTagName('input')[0].id;
				vrijednostiRedaka = vrijednostiRedaka +(idCelije+"="+vrijednostCelije)+"[,]";
				vrijednostiPrvogRetka [j] = vrijednostCelije;
				//alert(idCelije+" = "+vrijednostCelije);
		}
		}
		//alert(vrijednostiPrvogRetka)

		for(var i = 2; i< brojRedovaTabliceIspis-1;i++)
		{
		var brojStupacaRedovaTabliceIspis = document.getElementById('tablicaIspis').getElementsByTagName('tr')[i].getElementsByTagName('td');
		for(j=0;j<brojStupacaRedovaTabliceIspis.length;j++)
		{
			if(j==0)
				{

				}
			else if(j==1)
				{

				}
			else{
				vrijednostCelije=brojStupacaRedovaTabliceIspis[j].getElementsByTagName('input')[0].value;
				idCelije = brojStupacaRedovaTabliceIspis[j].getElementsByTagName('input')[0].id;
				vrijednostiRedaka = vrijednostiRedaka +(idCelije+"="+vrijednostCelije)+"[,]";
				if(document.getElementById(idCelije).className != "tablicaIzmjenaHolidayWeekDay cssPodnaslovi")
					{
				document.getElementById(idCelije).value = vrijednostiPrvogRetka [j];
					}
				else{}
				//vrijednostiPrvogRetka [j] = vrijednostCelije;
				//alert(idCelije+" = "+vrijednostCelije);
		}
		}
		}
	}
}
function otvori_novi_red()
{
	var broj;
    if (!document.getElementsByTagName || !document.createTextNode) return;
    var rows = document.getElementById('tablicaIspis').getElementsByTagName('tr');
    for (i = 0; i < rows.length; i++) {
        rows[i].onclick = function() {
            //alert(this.rowIndex);
            broj = this.rowIndex;
			/*var x=document.getElementById('tablicaIspis').insertRow(this.rowIndex+1);
			var y=x.insertCell(0);
			var z=x.insertCell(1);
			y.innerHTML="<td align='center' class='tablicaIzmjenaHolidayWeekDay cssPodnaslovi' id='${fn:replace(radniKalendar.datumUnosa ,'.' ,'')}''><span class='cssPodnaslovi'>k</span></td>";
			z.innerHTML="";*/

     		ispisi_red(broj);
        }
    }

    /*alert(broj);
	var x=document.getElementById('tablicaIspis').insertRow(broj);
	var y=x.insertCell(0);
		var z=x.insertCell(1);
	y.innerHTML="<td align='center' class='tablicaIzmjenaHolidayWeekDay cssPodnaslovi' id='${fn:replace(radniKalendar.datumUnosa ,'.' ,'')}''><span class='cssPodnaslovi'>k</span></td>";
	z.innerHTML="";*/
}
function ispisi_red(broj)
{
var x=document.getElementById('tablicaIspis').insertRow(broj+1);
			var y=x.insertCell(0);
			var z=x.insertCell(1);
			y.innerHTML="<td align='center' class='tablicaIzmjenaHolidayWeekDay cssPodnaslovi' id='${fn:replace(radniKalendar.datumUnosa ,'.' ,'')}''><span class='cssPodnaslovi'>k</span></td>";
			z.innerHTML="";
}

function nenazocnost_umanjivanje(id)
{

	var idredak = id.slice(4);
	var datumRedak = idredak.slice(0,2)+"/"+idredak.slice(2,4)+"/"+idredak.slice(4);
	var value_nenazocnostRV = document.getElementById("vr06"+id.slice(4)).value
	var fulldata_nenazocnostRV=null;
	var fulldata2_nenazocnostRV=null;
	var value_odobrena_nenazocnost = null;
	var fulldata_odobrena_nenazocnost = null
	var value_neodobrena_nenazocnost = null
	var fulldata_neodobrena_nenazocnost = null;
	var suma_odobreno_neodobreno = null;
	var fulldata_suma_odobreno_neodobreno = null
	var nazocnost = "1.00"
	//alert(document.getElementById("vr02"+idredak).value)
	if(document.getElementById("vr02"+idredak).value!="" && document.getElementById("vr02"+idredak).value!="00:00")
	{
	if(id.slice(0,4)== "vr10")
	{

		value_odobrena_nenazocnost = document.getElementById(id).value.replace(",",".");
		value_neodobrena_nenazocnost = sumDate(document.getElementById("vr22"+id.slice(4)).value.replace(",","."), document.getElementById("vr11"+id.slice(4)).value.replace(",","."))//zbraja rodiljni i trenutni neodobreni
	}
	if(id.slice(0,4)=="vr11")
		{
		value_neodobrena_nenazocnost = document.getElementById(id).value.replace(",",".");
		value_odobrena_nenazocnost = sumDate(document.getElementById("vr10"+id.slice(4)).value.replace(",","."), document.getElementById("vr22"+id.slice(4)).value.replace(",","."))
		}
	if(id.slice(0,4)=="vr22")
		{
		var temp_value_neodobrena_nenazocnost = document.getElementById("vr11"+id.slice(4)).value.replace(",",".")
		var temp_value_odobrena_nenazocnost = document.getElementById("vr10"+id.slice(4)).value.replace(",",".")
		var temp_vrijednost_doj=document.getElementById(id).value.replace(",",".");
		//alert(temp_vrijednost_doj.slice(0,3))
		if(temp_vrijednost_doj.slice(0,3)=='2.0')
			{
		if(temp_value_odobrena_nenazocnost=="0.00")
			{
			value_odobrena_nenazocnost = document.getElementById(id).value.replace(",",".");
			value_neodobrena_nenazocnost = document.getElementById("vr11"+id.slice(4)).value.replace(",",".")
			}
		else if(temp_value_neodobrena_nenazocnost=="0.00")
			{
			value_neodobrena_nenazocnost = document.getElementById(id).value.replace(",",".");
			value_odobrena_nenazocnost = document.getElementById("vr10"+id.slice(4)).value.replace(",",".")
			}
		else{
			value_odobrena_nenazocnost = document.getElementById(id).value.replace(",",".");
			value_neodobrena_nenazocnost = suma_odobreno_neodobreno = sumDate(document.getElementById("vr10"+id.slice(4)).value.replace(",","."), document.getElementById("vr11"+id.slice(4)).value.replace(",","."));
		}
			}
		else{
			value_neodobrena_nenazocnost = '0.00'
			value_odobrena_nenazocnost = '0.00'
			document.getElementById(id).value='0.00'
			msgboxWarning("Upozorenje",'Sati upisani kao stanka za dojenje djeteta mora biti 2.00 h dnevno',"warning");
		}
		}
	//alert(value_odobrena_nenazocnost+" -- "+value_neodobrena_nenazocnost)
	suma_odobreno_neodobreno = sumDate(value_odobrena_nenazocnost, value_neodobrena_nenazocnost);
	//alert(suma_odobreno_neodobreno);
	if(suma_odobreno_neodobreno.slice(suma_odobreno_neodobreno.indexOf(".")).length<=2)
		{
		fulldata_suma_odobreno_neodobreno = datumRedak+" 0"+suma_odobreno_neodobreno.replace(".",":0")+":00";
		}
	else{
		fulldata_suma_odobreno_neodobreno = datumRedak+" 0"+suma_odobreno_neodobreno.replace(".",":")+":00";
	}
	//alert((datumRedak+" 0"+value_nenazocnostRV.replace(".",":")+":00",datumRedak+" 08:00:00"));
	fulldata_nenazocnostRV = dateDiff(datumRedak+" 0"+value_nenazocnostRV.replace(".",":")+":00",datumRedak+" 08:00:00");
	fulldata2_nenazocnostRV = datumRedak+" 0"+fulldata_nenazocnostRV.replace(".",":")+":00";
	fulldata_odobrena_nenazocnost = datumRedak+" 0"+value_odobrena_nenazocnost.replace(".",":")+":00";
	fulldata_neodobrena_nenazocnost = datumRedak+" 0"+value_neodobrena_nenazocnost.replace(".",":")+":00";

	//alert(fulldata_nenazocnostRV);
	//alert(chackInsertTime(fulldata_nenazocnostRV, fulldata_suma_odobreno_neodobreno))
	if(chackInsertTime(fulldata_suma_odobreno_neodobreno,fulldata2_nenazocnostRV)=="true")
		{
		//alert(fulldata_suma_odobreno_neodobreno+" -- "+ fulldata_nenazocnostRV);
		document.getElementById("vr09"+id.slice(4)).value = dateDiff(fulldata_suma_odobreno_neodobreno, fulldata2_nenazocnostRV);
		}
	else
		{

		msgboxWarning("Upozorenje",'Zbroj vrijednosti odobrene, neodobrene nazočnosti i stanke za dojenje djeteta je veći od sati nenazočnosti u dnevnom rasporedu RV',"warning");
		document.getElementById(id).value='0.00'
		}
	//document.getElementById("vr09"+id).value = nazocnost;
	//alert(value_nenazocnostRV+" -- "+value_odobrena_nenazocnost+" -- "+value_neodobrena_nenazocnost);
	//alert(sumDate(value_odobrena_nenazocnost, value_neodobrena_nenazocnost));
	}
	else{
		if(document.getElementById("vr09"+id.slice(4)).value.replace(",",".")!="0.00")
		{

			var value_vr09 = document.getElementById("vr09"+id.slice(4)).value.replace(",",".");
			var value_vr10 = document.getElementById("vr10"+id.slice(4)).value.replace(",",".");
			var value_vr11 = document.getElementById("vr11"+id.slice(4)).value.replace(",",".");
			var value_vr22 = document.getElementById("vr22"+id.slice(4)).value.replace(",",".");
			if(value_vr22=="0.00" || value_vr22=="2.00")
			{
			var zbroj_vrijed=sumDate(sumDate(value_vr10, value_vr11), value_vr22);
			//alert(chackInsertTime(datumRedak+" 0"+zbroj_vrijed.replace(".",":")+":00",datumRedak+" 08:00:00"))
			if(chackInsertTime(datumRedak+" 0"+zbroj_vrijed.replace(".",":")+":00",datumRedak+" 08:00:00")=="true")

			{
			document.getElementById("vr09"+id.slice(4)).value=dateDiff(datumRedak+" 0"+zbroj_vrijed.replace(".",":")+":00",datumRedak+" 08:00:00");
			}
			else{
				msgboxWarning("Upozorenje",'Zbroj vrijednosti sati odobrenog, neodobrenog i stanke za dojenje djeteta je veći od sati nenazočnosti u dnevnom rasporedu RV',"warning");
				document.getElementById("vr09"+id.slice(4)).value="8.00";
				document.getElementById("vr10"+id.slice(4)).value="0.00";
				document.getElementById("vr11"+id.slice(4)).value="0.00";
				document.getElementById("vr22"+id.slice(4)).value="0.00";
			}
			}
			else{
				msgboxWarning("Upozorenje",'Za stanku za dojenje djeteta može se upisati 2.00 h',"warning");
				document.getElementById("vr09"+id.slice(4)).value="8.00";
				document.getElementById("vr10"+id.slice(4)).value="0.00";
				document.getElementById("vr11"+id.slice(4)).value="0.00";
				document.getElementById("vr22"+id.slice(4)).value="0.00";
			}
		}
		else if(document.getElementById("vr12"+id.slice(4)).value.replace(",",".")!="0.00")
			{
			var value_vr22 = document.getElementById("vr22"+id.slice(4)).value.replace(",",".");
			if( value_vr22=="2.00"){
				document.getElementById("vr12"+id.slice(4)).value="6.00";
			}
			else{
				msgboxWarning("Upozorenje",'Za stanku za dojenje djeteta može se upisati 2.00 h',"warning");
				document.getElementById("vr12"+id.slice(4)).value="8.00";
				document.getElementById("vr22"+id.slice(4)).value="0.00";
			}

			}
		else if(document.getElementById("vr23"+id.slice(4)).value.replace(",",".")!="0.00")
		{
		msgboxWarning("Upozorenje",'Sati stanke za dojenje djeteta se ne može kombinirati s satima plaćenog dopusta',"warning");
		document.getElementById("vr23"+id.slice(4)).value="8.00";
		document.getElementById("vr22"+id.slice(4)).value="0.00";
		}
		else if(document.getElementById("vr24"+id.slice(4)).value.replace(",",".")!="0.00")
		{
			var value_vr22 = document.getElementById("vr22"+id.slice(4)).value.replace(",",".");
			if( value_vr22=="2.00"){
				document.getElementById("vr24"+id.slice(4)).value="6.00";
			}
			else{
				msgboxWarning("Upozorenje",'Za stanku za dojenje djeteta može se upisati 2.00 h',"warning");
				document.getElementById("vr24"+id.slice(4)).value="8.00";
				document.getElementById("vr22"+id.slice(4)).value="0.00";
			}

			}
		else if(document.getElementById("vr17"+id.slice(4)).value.replace(",",".")!="0.00")
		{
		msgboxWarning("Upozorenje",'Sati stanke za dojenje djeteta se ne može kombinirati s satima bolovanja',"warning");
		document.getElementById("vr17"+id.slice(4)).value="8.00";
		document.getElementById("vr22"+id.slice(4)).value="0.00";
		}
		else if(document.getElementById("vr26"+id.slice(4)).value.replace(",",".")!="0.00")
		{
			var value_vr22 = document.getElementById("vr22"+id.slice(4)).value.replace(",",".");
			if( value_vr22=="2.00"){
				document.getElementById("vr26"+id.slice(4)).value="6.00";
			}
			else{
				msgboxWarning("Upozorenje",'Za stanku za dojenje djeteta može se upisati 2.00 h',"warning");
				document.getElementById("vr26"+id.slice(4)).value="8.00";
				document.getElementById("vr22"+id.slice(4)).value="0.00";
			}

			}
		else if(document.getElementById("vr27"+id.slice(4)).value.replace(",",".")!="0.00")
		{
			var value_vr22 = document.getElementById("vr22"+id.slice(4)).value.replace(",",".");
			if( value_vr22=="2.00"){
				document.getElementById("vr27"+id.slice(4)).value="6.00";
			}
			else{
				msgboxWarning("Upozorenje",'Za stanku za dojenje djeteta može se upisati 2.00 h',"warning");
				document.getElementById("vr27"+id.slice(4)).value="8.00";
				document.getElementById("vr22"+id.slice(4)).value="0.00";
			}

			}
		else if(document.getElementById("vr16"+id.slice(4)).value.replace(",",".")!="0.00")
		{
		var value_vr22 = document.getElementById("vr22"+id.slice(4)).value.replace(",",".");
		if( value_vr22=="2.00"){
			document.getElementById("vr16"+id.slice(4)).value="6.00";
		}
		else{
			msgboxWarning("Upozorenje",'Za stanku za dojenje djeteta može se upisati 2.00 h',"warning");
			document.getElementById("vr16"+id.slice(4)).value="8.00";
			document.getElementById("vr22"+id.slice(4)).value="0.00";
		}

		}

		else{
			msgboxWarning("Upozorenje",'Sati stanke za dojenje djeteta mora biti u kombinaciji s nekom drugom vrstom rada',"warning");
			document.getElementById("vr22"+id.slice(4)).value="8.00";
			//document.getElementById("vr22"+id.slice(4)).value="0.00";
		}
		//msgboxWarning("Upozorenje",'Unesena je odsutnost na dan :'+datumRedak+' i nije dozvoljen unos sati odobrene, neodobrene nenazočnosti i stanke za dojenje djeteta',"warning");
	}
}

function nenazocnost_umanjivanje_dnevno(id)
{
	//alert("NNNNN")
	var idredak = id.slice(5);
	var datumRedak = userData.dan_izmjene_dnevno.replace(/\./g,'/');
	//alert("--"+datumRedak+"--")
	var value_nenazocnostRV = document.getElementById("dvr06"+id.slice(5)).value
	var fulldata_nenazocnostRV=null;
	var fulldata2_nenazocnostRV=null;
	var value_odobrena_nenazocnost = null;
	var fulldata_odobrena_nenazocnost = null
	var value_neodobrena_nenazocnost = null
	var fulldata_neodobrena_nenazocnost = null;
	var suma_odobreno_neodobreno = null;
	var fulldata_suma_odobreno_neodobreno = null
	var nazocnost = "1.00"
		//alert(id)
		if(document.getElementById("dvr02"+idredak).value!="" && document.getElementById("dvr02"+idredak).value!="00:00")
		{
	if(id.slice(0,5)== "dvr10")
	{
		var temp_vrijednost_doj= document.getElementById("dvr22"+id.slice(5)).value.replace(",",".")
		value_odobrena_nenazocnost = document.getElementById(id).value.replace(",",".");
		value_neodobrena_nenazocnost = sumDate(document.getElementById("dvr22"+id.slice(5)).value.replace(",","."), document.getElementById("dvr11"+id.slice(5)).value.replace(",","."))
	}
	if(id.slice(0,5)=="dvr11")
		{
		value_neodobrena_nenazocnost = document.getElementById(id).value.replace(",",".");
		value_odobrena_nenazocnost = sumDate(document.getElementById("dvr10"+id.slice(5)).value.replace(",","."), document.getElementById("dvr22"+id.slice(5)).value.replace(",","."))
		}
	if(id.slice(0,5)=="dvr22")
	{
	var temp_value_neodobrena_nenazocnost = document.getElementById("dvr11"+id.slice(5)).value.replace(",",".")
	var temp_value_odobrena_nenazocnost = document.getElementById("dvr10"+id.slice(5)).value.replace(",",".")
	var temp_vrijednost_doj=document.getElementById(id).value.replace(",",".");
	//alert(temp_vrijednost_doj.slice(0,3))
	if(temp_vrijednost_doj.slice(0,3)=='2.0')
		{
	if(temp_value_odobrena_nenazocnost=="0.00")
		{
		value_odobrena_nenazocnost = document.getElementById(id).value.replace(",",".");
		value_neodobrena_nenazocnost = document.getElementById("dvr11"+id.slice(5)).value.replace(",",".")
		}
	else if(temp_value_neodobrena_nenazocnost=="0.00")
		{
		value_neodobrena_nenazocnost = document.getElementById(id).value.replace(",",".");
		value_odobrena_nenazocnost = document.getElementById("dvr10"+id.slice(5)).value.replace(",",".")
		}
	else{
		value_odobrena_nenazocnost = document.getElementById(id).value.replace(",",".");
		value_neodobrena_nenazocnost = suma_odobreno_neodobreno = sumDate(document.getElementById("dvr10"+id.slice(5)).value.replace(",","."), document.getElementById("dvr11"+id.slice(5)).value.replace(",","."));
	}
		}
	else{
		//alert("n")
		value_neodobrena_nenazocnost = '0.00'
		value_odobrena_nenazocnost = '0.00'
		document.getElementById(id).value='0.00'
		msgboxWarning("Upozorenje",'Sati upisani kao stanka za dojenje djeteta mora biti 2.00 h dnevno',"warning");
	}
	}
	//alert(value_odobrena_nenazocnost+" -- "+value_neodobrena_nenazocnost)
	suma_odobreno_neodobreno = sumDate(value_odobrena_nenazocnost, value_neodobrena_nenazocnost);
	//alert(suma_odobreno_neodobreno.slice(suma_odobreno_neodobreno.indexOf(".")).length);
	if(suma_odobreno_neodobreno.slice(suma_odobreno_neodobreno.indexOf(".")).length<=2)
		{
		fulldata_suma_odobreno_neodobreno = datumRedak+" 0"+suma_odobreno_neodobreno.replace(".",":0")+":00";
		}
	else{
		fulldata_suma_odobreno_neodobreno = datumRedak+" 0"+suma_odobreno_neodobreno.replace(".",":")+":00";
	}
	//alert(sumDate(value_odobrena_nenazocnost, value_neodobrena_nenazocnost));
	fulldata_nenazocnostRV = dateDiff(datumRedak+" 0"+value_nenazocnostRV.replace(".",":")+":00",datumRedak+" 08:00:00");
	fulldata2_nenazocnostRV = datumRedak+" 0"+fulldata_nenazocnostRV.replace(".",":")+":00";
	fulldata_odobrena_nenazocnost = datumRedak+" 0"+value_odobrena_nenazocnost.replace(".",":")+":00";
	fulldata_neodobrena_nenazocnost = datumRedak+" 0"+value_neodobrena_nenazocnost.replace(".",":")+":00";

	//alert(fulldata_nenazocnostRV);
	//alert(chackInsertTime(fulldata_nenazocnostRV, fulldata_suma_odobreno_neodobreno))
	if(chackInsertTime(fulldata_suma_odobreno_neodobreno,fulldata2_nenazocnostRV)=="true")
		{
		//alert(dateDiff(fulldata_suma_odobreno_neodobreno, fulldata_nenazocnostRV));
		document.getElementById("dvr09"+id.slice(5)).value = dateDiff(fulldata_suma_odobreno_neodobreno, fulldata2_nenazocnostRV);
		}
	else
		{
		msgboxWarning("Upozorenje",'Zbroj vrijednosti odobrene, neodobrene nazočnosti i stanke za dojenje djeteta je veći od sati nenazočnosti u dnevnom rasporedu RV',"warning");
		}
		}
		else{

			if(document.getElementById("dvr09"+id.slice(5)).value.replace(",",".")!="0.00")
			{

				var value_dvr09 = document.getElementById("dvr09"+id.slice(5)).value.replace(",",".");
				var value_dvr10 = document.getElementById("dvr10"+id.slice(5)).value.replace(",",".");
				var value_dvr11 = document.getElementById("dvr11"+id.slice(5)).value.replace(",",".");
				var value_dvr22 = document.getElementById("dvr22"+id.slice(5)).value.replace(",",".");
				if(value_dvr22=="0.00" || value_dvr22=="2.00")
				{
				var zbroj_vrijed=sumDate(sumDate(value_dvr10, value_dvr11), value_dvr22);
				//alert(chackInsertTime(datumRedak+" 0"+zbroj_vrijed.replace(".",":")+":00",datumRedak+" 08:00:00"))
				if(chackInsertTime(datumRedak+" 0"+zbroj_vrijed.replace(".",":")+":00",datumRedak+" 08:00:00")=="true")

				{
				document.getElementById("dvr09"+id.slice(5)).value=dateDiff(datumRedak+" 0"+zbroj_vrijed.replace(".",":")+":00",datumRedak+" 08:00:00");
				}
				else{
					msgboxWarning("Upozorenje",'Zbroj vrijednosti sati odobrenog, neodobrenog i stanke za dojenje djeteta je veći od sati nenazočnosti u dnevnom rasporedu RV',"warning");
					document.getElementById("dvr09"+id.slice(5)).value="8.00";
					document.getElementById("dvr10"+id.slice(5)).value="0.00";
					document.getElementById("dvr11"+id.slice(5)).value="0.00";
					document.getElementById("dvr22"+id.slice(5)).value="0.00";
				}
				}
				else{
					msgboxWarning("Upozorenje",'Za stanku za dojenje djeteta može se upisati 2.00 h',"warning");
					document.getElementById("dvr09"+id.slice(5)).value="8.00";
					document.getElementById("dvr10"+id.slice(5)).value="0.00";
					document.getElementById("dvr11"+id.slice(5)).value="0.00";
					document.getElementById("dvr22"+id.slice(5)).value="0.00";
				}
			}
			else if(document.getElementById("dvr12"+id.slice(5)).value.replace(",",".")!="0.00")
				{
				var value_dvr22 = document.getElementById("dvr22"+id.slice(5)).value.replace(",",".");
				if( value_dvr22=="2.00"){
					document.getElementById("dvr12"+id.slice(5)).value="6.00";
				}
				else{
					msgboxWarning("Upozorenje",'Za stanku za dojenje djeteta može se upisati 2.00 h',"warning");
					document.getElementById("dvr12"+id.slice(5)).value="8.00";
					document.getElementById("dvr22"+id.slice(5)).value="0.00";
				}

				}
			else if(document.getElementById("dvr23"+id.slice(5)).value.replace(",",".")!="0.00")
			{
			msgboxWarning("Upozorenje",'Sati stanke za dojenje djeteta se ne može kombinirati s satima plaćenog dopusta',"warning");
			document.getElementById("dvr23"+id.slice(5)).value="8.00";
			document.getElementById("dvr22"+id.slice(5)).value="0.00";
			}
			else if(document.getElementById("dvr24"+id.slice(5)).value.replace(",",".")!="0.00")
			{
				var value_dvr22 = document.getElementById("dvr22"+id.slice(5)).value.replace(",",".");
				if( value_dvr22=="2.00"){
					document.getElementById("dvr24"+id.slice(5)).value="6.00";
				}
				else{
					msgboxWarning("Upozorenje",'Za stanku za dojenje djeteta može se upisati 2.00 h',"warning");
					document.getElementById("dvr24"+id.slice(5)).value="8.00";
					document.getElementById("dvr22"+id.slice(5)).value="0.00";
				}

				}
			else if(document.getElementById("dvr17"+id.slice(5)).value.replace(",",".")!="0.00")
			{
			msgboxWarning("Upozorenje",'Sati stanke za dojenje djeteta se ne može kombinirati s satima bolovanja',"warning");
			document.getElementById("dvr17"+id.slice(5)).value="8.00";
			document.getElementById("dvr22"+id.slice(5)).value="0.00";
			}
			else if(document.getElementById("dvr26"+id.slice(5)).value.replace(",",".")!="0.00")
			{
				var value_dvr22 = document.getElementById("dvr22"+id.slice(5)).value.replace(",",".");
				if( value_dvr22=="2.00"){
					document.getElementById("dvr26"+id.slice(5)).value="6.00";
				}
				else{
					msgboxWarning("Upozorenje",'Za stanku za dojenje djeteta može se upisati 2.00 h',"warning");
					document.getElementById("dvr26"+id.slice(5)).value="8.00";
					document.getElementById("dvr22"+id.slice(5)).value="0.00";
					document.getElementById("dvr24"+id.slice(5)).value="0.00";
				}

				}
			else if(document.getElementById("dvr27"+id.slice(5)).value.replace(",",".")!="0.00")
			{
				var value_dvr22 = document.getElementById("dvr22"+id.slice(5)).value.replace(",",".");
				if( value_dvr22=="2.00"){
					document.getElementById("dvr27"+id.slice(5)).value="6.00";
				}
				else{
					msgboxWarning("Upozorenje",'Za stanku za dojenje djeteta može se upisati 2.00 h',"warning");
					document.getElementById("dvr27"+id.slice(5)).value="8.00";
					document.getElementById("dvr22"+id.slice(5)).value="0.00";
				}

				}
			else if(document.getElementById("dvr16"+id.slice(5)).value.replace(",",".")!="0.00")
			{
			var value_dvr22 = document.getElementById("dvr22"+id.slice(5)).value.replace(",",".");
			if( value_dvr22=="2.00"){
				document.getElementById("dvr16"+id.slice(5)).value="6.00";
			}
			else{
				msgboxWarning("Upozorenje",'Za stanku za dojenje djeteta može se upisati 2.00 h',"warning");
				document.getElementById("dvr16"+id.slice(5)).value="8.00";
				document.getElementById("dvr22"+id.slice(5)).value="0.00";
				document.getElementById("dvr24"+id.slice(5)).value="0.00";
			}

			}
			else{
				msgboxWarning("Upozorenje",'Sati stanke za dojenje djeteta mora biti u kombinaciji s nekom drugom vrstom rada',"warning");
				document.getElementById("dvr22"+id.slice(4)).value="8.00";
				document.getElementById("dvr24"+id.slice(4)).value="8.00";
				//document.getElementById("vr22"+id.slice(4)).value="0.00";
			}
			//msgboxWarning("Upozorenje",'Unesena je odsutnost na dan :'+datumRedak+' i nije dozvoljen unos sati odobrene, neodobrene nenazočnosti i stanke za dojenje djeteta',"warning");
		}

	//alert(value_nenazocnostRV+" -- "+value_odobrena_nenazocnost+" -- "+value_neodobrena_nenazocnost);
	//alert(sumDate(value_odobrena_nenazocnost, value_neodobrena_nenazocnost));
}
function prisIzvanRad_umanjivanje(id)
{

	var idredak = id.slice(4);

	var datumRedak = idredak.slice(0,2)+"/"+idredak.slice(2,4)+"/"+idredak.slice(4);
	var value_prisIzvanRad = document.getElementById("vr13"+id.slice(4)).value
	var prekov_odobreni = document.getElementById(id).value.replace(",",".")
	//alert(prekov_odobreni)
	//alert(document.getElementById("vr01"+id.slice(4)).value.replace(",",":")+" -- "+document.getElementById("vr02"+id.slice(4)).value.replace(",",":"))
	var stvarni_rad = dateDiff(datumRedak+" "+document.getElementById("vr01"+id.slice(4)).value.replace(",",":")+":00",datumRedak+" "+document.getElementById("vr02"+id.slice(4)).value.replace(",",":")+":00")
	//alert(stvarni_rad)
	var fulldata_stvarni_rad = null;
	var stvarni_prekovremeni_rad = null;
	var pris_izvan_rad = null
	var len = stvarni_rad.slice(0,stvarni_rad.indexOf(".")).length
	var len2 = prekov_odobreni.slice(0,prekov_odobreni.indexOf(".")).length
	//alert(stvarni_rad.slice(0,stvarni_rad.indexOf(".")).length)
	//alert(len+" -- "+len2)
	if(len<=1)
		{
			fulldata_stvarni_rad = datumRedak+" 0"+stvarni_rad.replace(".",":")+":00"
		}
	else{
		fulldata_stvarni_rad = datumRedak+" "+stvarni_rad.replace(".",":")+":00"
	}
	//alert(document.getElementById(id).className)
	if(document.getElementById(id).className!="tablicaIzmjenaHolidayWeekDay cssPodnaslovi")
		{
	stvarni_prekovremeni_rad = dateDiff(datumRedak+" 08:00:00",fulldata_stvarni_rad)
		}
	else{
		stvarni_prekovremeni_rad = dateDiff(datumRedak+" 00:00:00",fulldata_stvarni_rad)
		}
	//alert(stvarni_prekovremeni_rad +" -- "+fulldata_stvarni_rad)
	if(len2<=1)
		{
		pris_izvan_rad = dateDiff(datumRedak+" 0"+prekov_odobreni.replace(".",":")+":00",datumRedak+" 0"+stvarni_prekovremeni_rad.replace(".",":")+":00")
		}
	else{
		pris_izvan_rad = dateDiff(datumRedak+" "+prekov_odobreni.replace(".",":")+":00",datumRedak+" 0"+stvarni_prekovremeni_rad.replace(".",":")+":00")
	}
	//alert(parseInt(pris_izvan_rad))
	if(pris_izvan_rad.indexOf("-")>0 || parseInt(pris_izvan_rad)<0)
		{
		msgboxWarning("Upozorenje",'Sati prekovremenog rada veći su od sati prisutnosti izvan radnog vremena',"warning");
		document.getElementById("vr13"+id.slice(4)).value=stvarni_prekovremeni_rad
		document.getElementById("vr14"+id.slice(4)).value="0.00"
		}
	else{
		document.getElementById("vr13"+id.slice(4)).value=pris_izvan_rad
	//alert(pris_izvan_rad)
	}
	/*var fulldata_nenazocnostRV=null;
	var fulldata2_nenazocnostRV=null;
	var value_odobrena_nenazocnost = null;
	var fulldata_odobrena_nenazocnost = null
	var value_neodobrena_nenazocnost = null
	var fulldata_neodobrena_nenazocnost = null;
	var suma_odobreno_neodobreno = null;
	var fulldata_suma_odobreno_neodobreno = null
	var nazocnost = "1.00"
	if(id.slice(0,4)== "vr10")
	{
		value_odobrena_nenazocnost = document.getElementById(id).value.replace(",",".");
		value_neodobrena_nenazocnost = document.getElementById("vr11"+id.slice(4)).value
	}

}

function prisIzvanRad_umanjivanje_dnevno(id)
{


	//alert("nn")
	/*var idredak = id.slice(5);

	var datumRedak = userData.dan_izmjene_dnevno.replace(/\./g,'/');
	var value_prisIzvanRad = document.getElementById("dvr13"+id.slice(5)).value
	var prekov_odobreni = document.getElementById(id).value.replace(",",".")
	alert(prekov_odobreni)
	alert(document.getElementById("dvr01"+id.slice(5)).value.replace(",",":")+" -- "+document.getElementById("dvr02"+id.slice(5)).value.replace(",",":"))
	var stvarni_rad = dateDiff(datumRedak+" "+document.getElementById("dvr01"+id.slice(5)).value.replace(",",":")+":00",datumRedak+" "+document.getElementById("dvr02"+id.slice(5)).value.replace(",",":")+":00")
	var fulldata_stvarni_rad = null;
	var stvarni_prekovremeni_rad = null;
	var pris_izvan_rad = null
	var len = stvarni_rad.slice(0,stvarni_rad.indexOf(".")).length
	var len2 = prekov_odobreni.slice(0,prekov_odobreni.indexOf(".")).length
	//alert(stvarni_rad.slice(0,stvarni_rad.indexOf(".")).length)
	if(len<=1)
		{
			fulldata_stvarni_rad = datumRedak+" 0"+stvarni_rad.replace(".",":")+":00"
		}
	else{
		fulldata_stvarni_rad = datumRedak+" "+stvarni_rad.replace(".",":")+":00"
	}
	stvarni_prekovremeni_rad = dateDiff(datumRedak+" 08:00:00",fulldata_stvarni_rad)
	if(len2<=1)
		{
		pris_izvan_rad = dateDiff(datumRedak+" 0"+prekov_odobreni.replace(".",":")+":00",datumRedak+" 0"+stvarni_prekovremeni_rad.replace(".",":")+":00")
		}
	else{
		pris_izvan_rad = dateDiff(datumRedak+" "+prekov_odobreni.replace(".",":")+":00",datumRedak+" 0"+stvarni_prekovremeni_rad.replace(".",":")+":00")
	}

	if(pris_izvan_rad.indexOf("-")>0)
		{
		msgboxWarning("Upozorenje",'Sati prekovremenog rada veći su od sati prisutnosti izvan radnog vremena',"warning");
		document.getElementById("dvr13"+id.slice(5)).value=stvarni_prekovremeni_rad
		document.getElementById("dvr14"+id.slice(5)).value="0.00"
		}
	else{
		document.getElementById("dvr13"+id.slice(5)).value=pris_izvan_rad
	alert(pris_izvan_rad)
	}

	//document.getElementById("vr09"+id).value = nazocnost;
	//alert(value_nenazocnostRV+" -- "+value_odobrena_nenazocnost+" -- "+value_neodobrena_nenazocnost);
	//alert(sumDate(value_odobrena_nenazocnost, value_neodobrena_nenazocnost));*/
}

function bolovanjeVikend(id)
{
	var vrijednostBolovanja = document.getElementById("vr17"+id.slice(4)).value
	document.getElementById("vr01"+id.slice(4)).value =""
	document.getElementById("vr02"+id.slice(4)).value =""
	document.getElementById("vr03"+id.slice(4)).value ="0.00"
	document.getElementById("vr04"+id.slice(4)).value ="00:00"
	document.getElementById("vr05"+id.slice(4)).value ="00:00"
	document.getElementById("vr06"+id.slice(4)).value ="0.00"
	document.getElementById("vr13"+id.slice(4)).value ="0.00"
	document.getElementById("vr14"+id.slice(4)).value ="0.00"

	if(vrijednostBolovanja.replace(",",".") == "0.00")
	{
		if(document.getElementById("vr03"+id.slice(4)).value =="0.00")
			{
				document.getElementById("vr18"+id.slice(4)).value ="8.00"
			}
		else{
			document.getElementById("vr18"+id.slice(4)).value ="0.00"
			document.getElementById("vr22"+id.slice(4)).value ="0.00"
				document.getElementById("vr24"+id.slice(4)).value ="0.00"
		}
			}
	else{
	document.getElementById("vr18"+id.slice(4)).value ="0.00"
		document.getElementById("vr22"+id.slice(4)).value ="0.00"
			document.getElementById("vr24"+id.slice(4)).value ="0.00"
	}
	}

function porodiljniVikend(id)
{
	var vrijednostPorodiljnjeg = document.getElementById("vr22"+id.slice(4)).value
	document.getElementById("vr01"+id.slice(4)).value =""
	document.getElementById("vr02"+id.slice(4)).value =""
	document.getElementById("vr03"+id.slice(4)).value ="0.00"
	document.getElementById("vr04"+id.slice(4)).value ="00:00"
	document.getElementById("vr05"+id.slice(4)).value ="00:00"
	document.getElementById("vr06"+id.slice(4)).value ="0.00"
	document.getElementById("vr13"+id.slice(4)).value ="0.00"
	document.getElementById("vr14"+id.slice(4)).value ="0.00"
		//alert(vrijednostPorodiljnjeg.replace(",","."))
	if(vrijednostPorodiljnjeg.replace(",",".") == "2.00" || vrijednostPorodiljnjeg.replace(",",".") == "0.00" || vrijednostPorodiljnjeg.replace(",",".") == "8.00")
	{
	if(vrijednostPorodiljnjeg.replace(",",".") == "0.00")
	{
		if(document.getElementById("vr03"+id.slice(4)).value =="0.00")
			{
				document.getElementById("vr18"+id.slice(4)).value ="8.00"
			}
		else{
			document.getElementById("vr18"+id.slice(4)).value ="0.00"
				document.getElementById("vr17"+id.slice(4)).value ="0.00"
					document.getElementById("vr24"+id.slice(4)).value ="0.00"
		}
			}
	else if (vrijednostPorodiljnjeg.replace(",",".") == "2.00")
		{
		if(document.getElementById("vr03"+id.slice(4)).value =="0.00")
		{
			document.getElementById("vr18"+id.slice(4)).value ="6.00"
		}
	else{
		document.getElementById("vr18"+id.slice(4)).value ="0.00"
			document.getElementById("vr17"+id.slice(4)).value ="0.00"
				document.getElementById("vr24"+id.slice(4)).value ="0.00"
	}
		}
	else{
	document.getElementById("vr18"+id.slice(4)).value ="0.00"
		document.getElementById("vr17"+id.slice(4)).value ="0.00"
			document.getElementById("vr24"+id.slice(4)).value ="0.00"
	}
	}
	else{
		msgboxWarning("Upozorenje",'Uneseni fond sati nije ispravan za rodiljni, roditeljski dopust i druga prava na blagdan',"warning");
	}
	}
function neplaceniDopustBlagdan(id)
{

	var neplaceniDopustBlagdan = document.getElementById("vr24"+id.slice(4)).value
	document.getElementById("vr01"+id.slice(4)).value =""
	document.getElementById("vr02"+id.slice(4)).value =""
	document.getElementById("vr03"+id.slice(4)).value ="0.00"
	document.getElementById("vr04"+id.slice(4)).value ="00:00"
	document.getElementById("vr05"+id.slice(4)).value ="00:00"
	document.getElementById("vr06"+id.slice(4)).value ="0.00"
	document.getElementById("vr13"+id.slice(4)).value ="0.00"
	document.getElementById("vr14"+id.slice(4)).value ="0.00"
		//alert(vrijednostPorodiljnjeg.replace(",","."))
	if(neplaceniDopustBlagdan.replace(",",".") == "0.00" || neplaceniDopustBlagdan.replace(",",".") == "8.00")
	{
	if(neplaceniDopustBlagdan.replace(",",".") == "0.00")
	{
		if(document.getElementById("vr03"+id.slice(4)).value =="0.00")
			{
				document.getElementById("vr18"+id.slice(4)).value ="8.00"
			}
		else{
			document.getElementById("vr18"+id.slice(4)).value ="0.00"
				document.getElementById("vr17"+id.slice(4)).value ="0.00"
					document.getElementById("vr22"+id.slice(4)).value ="0.00"
		}
			}
	else{
	document.getElementById("vr18"+id.slice(4)).value ="0.00"
		document.getElementById("vr17"+id.slice(4)).value ="0.00"
			document.getElementById("vr22"+id.slice(4)).value ="0.00"
	}
	}
	else{
		msgboxWarning("Upozorenje",'Uneseni fond sati nije ispravan na neplaćeni dopust na blagdan',"warning");
	}
	}
function rk_cursor(elem)
{
	document.getElementById(elem).select();
	var polje = elem
	//alert("polje :"+polje)
	document.onkeyup = KeyCheck;
}


$(document).ready(
    function(){



    	if(stranicaId == "unos_referentJSP")
    		{
    		setSifraOJCombo();
    		setBrojZaposlenikaCombo();
    		setPeridOdCombo()
    		setPeridDoCombo()


    		if(userData.tablica_evid_view == "izmjena")
    			{

    			//---------------------------------------------------------------

    			//---------------------------------------------------------------
    			//var idTable = document.getElementById("tablicaIspis");
    			//var idRows = idTable.rows;
    			//alert(idRows.getElementsByTagName("td").item(1))
    			//var idRows = idTable.getElementsByTagName("tr").item(1);
    			//alert(" - - "+idRows.rows.length);
    			//alert(idTable.rows.length);
    			$("#izmjena_referent").attr('class','botunHide');
    			$("#ispis_referent").attr('class','botunHide');
    			$("#potvrdi_referent").attr('class','wpfBlueButton2');
    			$("#nazad_referent").attr('class','wpfBlueButton2');
    			$("#auto_referent").attr('class','wpfBlueButton2');

    			/*alert(userData.string_ukupne_vrijednosti)
    			if(userData.string_ukupne_vrijednosti)
    			{
    				var ukupnosSati = new Array();
    				ukupnoSati = userData.string_ukupne_vrijednosti.split("|");

    				for(var i = 0;i<=ukupnoSati.length;i++)
    				{
    					var izmjenaSifraRada = ukupnoSati[i].slice(0,ukupnoSati[i].indexOf(":"));
    					var izmjenaUkupnaVrijednost = ukupnoSati[i].slice(ukupnoSati[i].indexOf(":")+":".length);

    					document.getElementById("iz"+izmjenaSifraRada).innerHTML = izmjenaUkupnaVrijednost;
    				}
    			}*/

    			}
    		if(userData.tablica_evid_view == "ispis")
			{
			$("#ispis_referent").attr('class','botunHide');
			$("#izmjena_referent").attr('class','botunHide');
			$("#tisak_referent").attr('class','wpfBlueButton2');
			$("#tisak_inspektor").attr('class','wpfBlueButton2');
			$("#nazad_referent").attr('class','wpfBlueButton2');

			if(userData.string_ukupne_vrijednosti)
			{
				var ukupnosSati = new Array();
				ukupnoSati = userData.string_ukupne_vrijednosti.split("|");

				for(var i = 0;i<=ukupnoSati.length;i++)
				{
					var izmjenaSifraRada = ukupnoSati[i].slice(0,ukupnoSati[i].indexOf(":"));
					var izmjenaUkupnaVrijednost = ukupnoSati[i].slice(ukupnoSati[i].indexOf(":")+":".length);

					document.getElementById("is"+izmjenaSifraRada).innerHTML = izmjenaUkupnaVrijednost;
				}
			}

			}
    		}
    	if(starnicaId = "djelatnikRKJSP")
    		{
    		if(document.getElementById("pocetakRadaSati") != null)
    		{
    	document.getElementById("pocetakRadaSati").value = userData.trans_pocetak_rada;
    	document.getElementById("pocetakRadaMin").value = "00";
    	document.getElementById("zavrsetakRadaSati").value = userData.trans_zavrsetak_rada;
    	document.getElementById("zavrsetakRadaMin").value = "00";
    		}
    		}


    });



