package hr.kingict.project.plinacro.evidencijaradaportlet;

import static hr.kingict.project.plinacro.evidencijaradaportlet.param.Const.*;
import hr.kingict.framework.common.BaseFrameworkException;
import hr.kingict.framework.common.BusinessException;
import hr.kingict.framework.common.context.ApplicationContextFactory;
import hr.kingict.framework.common.context.IApplicationContext;
import hr.kingict.framework.common.context.ISessionContext;
import hr.kingict.framework.common.data.DataCollection;
import hr.kingict.framework.common.manager.cache.KCacheException;
import hr.kingict.framework.common.manager.database.DatabaseException;
import hr.kingict.framework.common.manager.database.ITransaction;
import hr.kingict.framework.common.manager.log.ILogManager;
import hr.kingict.framework.common.manager.module.ModuleException;
import hr.kingict.framework.common.purequery.BeanDataHandler;
import hr.kingict.framework.common.utility.UtilConsole;
import hr.kingict.framework.common.utility.UtilDate;
import hr.kingict.framework.common.utility.UtilOther;
import hr.kingict.framework.common.utility.UtilSerialization;
import hr.kingict.framework.common.utility.UtilTypeConversion;
import hr.kingict.project.plinacro.evidencijaradaportlet.model.EvidencijaRadaPortletSessionBean;
import hr.kingict.project.plinacro.evidencijaradaportlet.model.Korisnik;
import hr.kingict.project.plinacro.evidencijaradaportlet.model.OrgJedinica;
import hr.kingict.project.plinacro.evidencijaradaportlet.model.OrgJedinicaRadniKalendar;
import hr.kingict.project.plinacro.evidencijaradaportlet.model.PrisutnostZaposlenikPovijest;
import hr.kingict.project.plinacro.evidencijaradaportlet.model.RadniKalendarNew;
import hr.kingict.project.plinacro.evidencijaradaportlet.model.StatusPrisutnost;
import hr.kingict.project.plinacro.evidencijaradaportlet.model.StatusPrisutnostiPoSektorima;
import hr.kingict.project.plinacro.evidencijaradaportlet.model.StatusPrisutnostiSkupno;
import hr.kingict.project.plinacro.evidencijaradaportlet.model.UkupnoSati;
import hr.kingict.project.plinacro.evidencijaradaportlet.model.VrstaRada;
import hr.kingict.project.plinacro.evidencijaradaportlet.model.Zaposlenik;
import hr.kingict.project.plinacro.evidencijaradaportlet.model.gen.PrisutnostVrstaRada;
import hr.kingict.project.plinacro.evidencijaradaportlet.param.Const;
import hr.kingict.project.plinacro.evidencijaradaportlet.util.UtilEvidencijaRada;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.sql.RowSet;

/**
 * A sample portlet based on GenericPortlet.
 */
public class EvidencijaRadaPortlet extends GenericPortlet {

	IApplicationContext ctxA = null;
	public static final String JSP_FOLDER = "/_EvidencijaRada/jsp/";    // JSP folder name
	public static final String VIEW_JSP = "EvidencijaRadaPortletView";         // JSP file name to be rendered on the view mode
	public static final String SESSION_BEAN = "EvidencijaRadaPortletSessionBean";  // Bean name for the portlet session
	public static final String FORM_SUBMIT = "EvidencijaRadaPortletFormSubmit";   // Action name for submit form
	public static final String FORM_TEXT = "EvidencijaRadaPortletFormText";     // Parameter name for the text input
	private String moduleName = null;
	private BeanDataHandler beanDataHandler = null;

	/**
	 * @see javax.portlet.Portlet#init()
	 */
	public void init() throws PortletException{
		super.init();
		try {
			moduleName = EvidencijaRadaPortletModule.NAME.toLowerCase();
			ctxA = ApplicationContextFactory.getCurrentContext();
			beanDataHandler = new BeanDataHandler(ctxA);
			cacheLoadSnippet();
			initBeans();
		} catch (Exception e) {
			throw new PortletException(e.toString(), e);
		}
	}

	private void cacheLoadSnippet() throws KCacheException {
		List<String> moduleIds = null;
		moduleIds = new ArrayList<String>();
		moduleIds.add(EvidencijaRadaPortletModule.NAME.toLowerCase());
		ctxA.getCacheManager().initCaches(moduleIds);
	}

	public void initBeans() throws BaseFrameworkException {
		DataCollection dc = ctxA.getDataBeanManager().getDataCollection();
		CacheBeanFiller cacheBeanFiller = new CacheBeanFiller(ctxA);
		Map<String, PrisutnostVrstaRada> mVrsteRada = cacheBeanFiller.fillVrsteRada();
		dc.add(VRSTE_RADA, mVrsteRada);
		dc.add(VRSTE_RADA_ALT, ctxA.getCacheManager().getCache("PrisutnostVrstaRadaAltCache").convertToMap());
		dc.add(VRSTE_RADA_VEZANE, ctxA.getCacheManager().getCache("PrisutnostVrstaRadaVezaneCache").convertToMap());
	}

	/**
	 * Serve up the <code>view</code> mode.
	 *
	 * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
	 */
	public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		ISessionContext ctxS = null;
		ILogManager log = null;

		try {

			System.out.println("------------- :: "+request);
			ctxS = UtilEvidencijaRada.getSessionContext(request);
			Map<String, Object> userData = UtilEvidencijaRada.getUserData(ctxS);
			log = ctxS.getLogManager();
			log.debug("+++EvidencijaPortlet doView");

			boolean dataInitialized = UtilTypeConversion.booleanToPrimitiveBoolean(userData.get(TRANS_DATA_INITIALIZED));
			if (!dataInitialized) {
				initData(ctxS);
			}
			else {}

			request.setAttribute(BEAN_KORISNIK, (Korisnik) ctxS.getDataBeanManager().getDataCollection().getObject(
					BEAN_KORISNIK));
			response.setContentType(request.getResponseContentType());
			PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(getJspFilePath(request, VIEW_JSP));
			rd.include(request,response);

		} catch (Exception e) {
			// TODO: handle exception
		}
		finally{}
	}

	/**
	 * Process an action request.
	 *
	 * @see javax.portlet.Portlet#processAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
	 */
	public void processAction(ActionRequest request, ActionResponse response) throws PortletException, java.io.IOException {
		ISessionContext ctxS = null;
		ITransaction trx = null;
		ResultSet rs = null;
		ILogManager log = null;
		Blob dataDumpBlob = null;
		byte[] barr = null;
		System.out.println("PROCESS ACTION");
		String prikazTablice = "nesto";

		List<RadniKalendarNew> lzaposlenikRadniKalendarInt= new ArrayList<RadniKalendarNew>();
		try {
			ctxS = UtilEvidencijaRada.getSessionContext(request);
			Map<String, Object> userData = UtilEvidencijaRada.getUserData(ctxS);
			log = ctxS.getLogManager();
			List<Object> parm = new ArrayList<Object>();
			String odabirStranica = null;

			log.debug("+++EvidencijaPortlet processAction");

			userData.put(TRANS_TABLICA_EVID_VIEW, prikazTablice);
			UtilEvidencijaRada.printRequestParameters(request, log);
			String command = request.getParameter(COMMAND);
			System.out.println("------------------- "+command+" ------------sdasdasdasdasd-----------");
			String periodOd = request.getParameter("date");
			String periodDo = request.getParameter("date1");
			String danIzmjeneDnevno = request.getParameter("dateDnevno");
			String commandNavigation = request.getParameter(COMMAND_NAVIGATION);
			if (commandNavigation != null && commandNavigation.length() > 0) {
				clearTransientData(userData);
				userData.put(COMMAND_NAVIGATION, commandNavigation);
				if (COMMAND_MENU_UPRAVLJANJE.equals(commandNavigation)) {
					odabirStranica = VIEW_JSP_UPRAVLJANJE;
				} else if (COMMAND_MENU_EVIDENCIJA.equals(commandNavigation)) {
					odabirStranica = VIEW_JSP_EVIDENCIJA;
				}
				else if (COMMAND_MENU_DNEVNIUNOS.equals(commandNavigation)) {
				odabirStranica = VIEW_JSP_DNEVNIUNOS;
				} else if (COMMAND_MENU_PRIJEPIS.equals(commandNavigation)) {
					odabirStranica = VIEW_JSP_PRIJEPIS;
				} else if (COMMAND_MENU_ODOBRISVE.equals(commandNavigation)) {
					odabirStranica = VIEW_JSP_ODOBRISVE;
				} else if (COMMAND_MENU_POVIJEST.equals(commandNavigation)) {
					odabirStranica = VIEW_JSP_POVIJEST;
				}
				System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOOOOO"+odabirStranica);
				userData.put(TRANS_ODABIR_STRANICE, odabirStranica);
			}
			int intObracunURaduCount = (Integer) userData.get(TRANS_OBRACUN_U_RADU_COUNT);
			if (intObracunURaduCount == 0) {
				if (command == null
						|| (!command.startsWith(PREFIX_COMMAND_UPRAVLJANJE) && !command.startsWith(PREFIX_COMMAND_POVIJEST) && !command.startsWith(PREFIX_COMMAND_EVID)&& !command.startsWith(PREFIX_COMMAND_REFERENT)&& !COMMAND_PRIJEPIS_IZVJESTAJ
								.equals(command)))
					return;
			}
			String prijenos = request.getParameter("zaposlenikList");
			System.out.println("--------- Zaposlenik :::::::: ="+prijenos+"=");
			String sifraOJ = request.getParameter("sifraOJ");
			String brojZaposlenika = null;
			String ugovorZaposlenika = null;
			System.out.println("Komanda : "+command);
			System.out.println("datum pocetaka iz jdata :"+userData.get(TRANS_PERIOD_OD));
			System.out.println("datum zavrĹˇetka iz jdata :"+userData.get(TRANS_PERIOD_DO));

			if(command!=null)
			{
			if(prijenos!=null && !prijenos.equals(""))
			{

			brojZaposlenika = request.getParameter("zaposlenikList").substring(0, request.getParameter("zaposlenikList").indexOf("/"));
			ugovorZaposlenika = request.getParameter("zaposlenikList").substring(request.getParameter("zaposlenikList").indexOf("/")+1);
			}
			else{
				if(command.equals(COMMAND_EVID_ZAPOSLENIK_SELECT))
				{
				brojZaposlenika = userData.get(TRANS_BROJZAPOSLENIKA).toString();
				ugovorZaposlenika = userData.get(TRANS_ZAP_UGOVOR).toString();
				}
			}
			}
			System.out.println("--------- Zaposlenik :::::::: "+brojZaposlenika+" -- "+ugovorZaposlenika);
			System.out.println("--------- Zaposlenik :::::::: "+prijenos);
			System.out.println("--------- ZAPOSLENIK ::::::::"+userData.get(TRANS_BROJZAPOSLENIKA)+" / "+userData.get(TRANS_ZAP_UGOVOR));

			Zaposlenik zaposlenik = null;
			List<Zaposlenik> lZaposlenik = (List<Zaposlenik>) userData.get(TRANS_LIST_ZAPOSLENIK);
			List<OrgJedinicaRadniKalendar> lzaposlenikRadniKalendar = (List<OrgJedinicaRadniKalendar>)userData.get(TRANS_LIST_ZAPOSLENIK_RADNI_KALENDAR_DNEVNO);
			String mjesecObracuna = (String) userData.get(TRANS_MJESEC_OBRACUNA);
			Integer obracunskiPeriod = (Integer) userData.get(TRANS_OBRACUNSKI_PERIOD);
			String mjesecObracunaHistory = (String) userData.get(TRANS_MJESEC_OBRACUNA_HISTORY);
			Integer obracunskiPeriodHistory = (Integer) userData.get(TRANS_OBRACUNSKI_PERIOD_HISTORY);
			String podaciUBazi = "0";
			java.sql.Date datumDohvata = (java.sql.Date) userData.get(TRANS_DATUM_DOHVATA);
			java.sql.Date datumDohvataHistory = (java.sql.Date) userData.get(TRANS_DATUM_DOHVATA_HISTORY);
			List<String> lUserOJs = (List<String>) userData.get(TRANS_USER_OJS);

			if (sifraOJ == null || sifraOJ.length() == 0)
				sifraOJ = (String) userData.get(TRANS_SIFRAOJ);
			userData.put(TRANS_SIFRAOJ, sifraOJ);

			trx = ctxA.getDatabaseManager().createTransaction();


			if (COMMAND_POVIJEST_OBRACUN_SELECT.equals(command)) {
				List<OrgJedinica> lOJHistory = null;
				String mjesecObracunaIObracunskiPeriodHistory = request.getParameter("mjesecObracunaIObracunskiPeriod");
				mjesecObracunaHistory = mjesecObracunaIObracunskiPeriodHistory.substring(0, 6);
				obracunskiPeriodHistory = new Integer(mjesecObracunaIObracunskiPeriodHistory.substring(7,
						mjesecObracunaIObracunskiPeriodHistory.length()));
				//mjesecObracunaHistory = request.getParameter("mjesecObracuna");
				//obracunskiPeriodHistory = new Integer(request.getParameter("obracunskiPeriod"));
				int yearHistory = Integer.parseInt(mjesecObracunaHistory.substring(0, 4));
				int monthHistory = Integer.parseInt(mjesecObracunaHistory.substring(4));
				datumDohvataHistory = new java.sql.Date(UtilDate.lastDayOfMonth(yearHistory, monthHistory).getTime());
				lOJHistory = sqlSnippetOJSelect(trx, rs, parm, lUserOJs, datumDohvataHistory);
				clearTransientData(userData);
				userData.put(TRANS_MJESEC_OBRACUNA_HISTORY, mjesecObracunaHistory);
				userData.put(TRANS_OBRACUNSKI_PERIOD_HISTORY, obracunskiPeriodHistory);
				userData.put(TRANS_DATUM_DOHVATA_HISTORY, datumDohvataHistory);
				userData.put(TRANS_LIST_NAZIV_OJ_HISTORY, lOJHistory);
				kludgeSnippet(userData, mjesecObracunaHistory, true);

			} else if (COMMAND_EVID_OJ_SELECT.equals(command)) {
				lZaposlenik = sqlSnippetRadniciUOJSelect(trx, rs, parm, mjesecObracuna, obracunskiPeriod, sifraOJ,
						datumDohvata);
				userData.put(TRANS_LIST_ZAPOSLENIK, lZaposlenik);
			} else if (COMMAND_POVIJEST_OJ_SELECT.equals(command)) {
				lZaposlenik = sqlSnippetRadniciUOJSelect(trx, rs, parm, mjesecObracunaHistory, obracunskiPeriodHistory,
						sifraOJ, datumDohvataHistory);
				userData.put(TRANS_LIST_ZAPOSLENIK, lZaposlenik);
			}

			else if (COMMAND_EVID_OJ_SELECT_DNEVNO.equals(command))
			{
				System.out.println("nesto"+userData.get(TRANS_SIFRAOJ));
				System.out.println("------------------ ");
				if(danIzmjeneDnevno.length()!= 0)
				{
					userData.put(TRANS_TABLICA_EVID_DNEVNO_VIEW, "izmjena_dnevno");
					userData.put(TRANS_DAN_IZMJENE_DNEVNO, danIzmjeneDnevno);
					userData.put(TRANS_OJ_IZMJENE_DNEVNO, userData.get(TRANS_SIFRAOJ));
				}
				else{userData.put(TRANS_TABLICA_EVID_DNEVNO_VIEW, "nesto");}
				//System.out.println(danIzmjeneDnevno.substring(6)+"-"+danIzmjeneDnevno.substring(3, 5)+"-"+danIzmjeneDnevno.substring(0,2));
				java.sql.Date datumDohvataDnevno = java.sql.Date.valueOf(danIzmjeneDnevno.substring(6)+"-"+danIzmjeneDnevno.substring(3, 5)+"-"+danIzmjeneDnevno.substring(0,2));
				log.debug("MAP CODE > -- sqlSnippetRadniciUOJSelect - dohvat radnika iz baze u odabranoj organizacijskoj jedinici (retrieval from a database of workers in the selected organizational unit)");
				lZaposlenik = sqlSnippetRadniciUOJDnevnoSelect(trx, rs, parm,  sifraOJ,datumDohvata);
				log.debug("MAP CODE > -- sqlSnippetRadniciUOJSelect end --");
				//System.out.println(" ----- "+lZaposlenik);
				userData.put(TRANS_LIST_ZAPOSLENIK, lZaposlenik);
				lzaposlenikRadniKalendar = sqlSnippetRadniKalendarRadnikaUOJSelect(trx,rs,parm,sifraOJ,datumDohvataDnevno,lZaposlenik,danIzmjeneDnevno,userData);
				System.out.println(" -------------- "+lzaposlenikRadniKalendar.size());
				userData.put(TRANS_LIST_ZAPOSLENIK_RADNI_KALENDAR_DNEVNO, lzaposlenikRadniKalendar);
			}


			else if (COMMAND_REFERENT_IZMJENA.equals(command))
			{

				System.out.println(userData.get(TRANS_BROJZAPOSLENIKA));
				//System.out.println("TEST IZMJENA ----------------- ");
				if(userData.get(TRANS_BROJZAPOSLENIKA).toString().length()==0)
					{
						clearTransientBadEnter(userData);
					}
				else
					{
					if(periodOd == null)
					{
						System.out.println("Datum ako je periodOd null"+userData.get(TRANS_PERIOD_OD));
						periodOd = userData.get(TRANS_PERIOD_OD).toString();
						periodDo = userData.get(TRANS_PERIOD_DO).toString();
					}
						Boolean checkDataValue = true;
						prikazTablice = "izmjena";
						userData.put(TRANS_TABLICA_EVID_VIEW, prikazTablice);

							if(periodDo.length() == 0)
								{
									periodDo=periodOd;
								}
							//else{}
							else if (periodOd.length() == 0)
								{
									periodOd=periodDo;
								}
							else{}
							if(periodOd.length() == 0 || periodDo.length() == 0)
								{
									periodOd = "02.11.2010";
									periodDo = "01.11.2010";
									//checkDataValue = false;
								}
							else{}


							//System.out.println("OD :"+periodOd+" DO : "+periodDo);
							String IDzaposlenikUnos = userData.get(TRANS_BROJZAPOSLENIKA).toString(); // dohvatiti id zaposlenika ulogiranog u aplikaciju
							trx = ctxA.getDatabaseManager().createTransaction();

							try
								{
									String transDataPeriodOd = periodOd.substring(6, 10)+"-"+periodOd.substring(3, 5)+"-"+periodOd.substring(0,2);
									String transDataPeriodDo = periodDo.substring(6, 10)+"-"+periodDo.substring(3, 5)+"-"+periodDo.substring(0,2);

									Calendar pocetniDatumOdabira = Calendar.getInstance();
									java.sql.Date sqlPeriodOdTemp = java.sql.Date.valueOf(transDataPeriodOd);
									pocetniDatumOdabira.setTime(sqlPeriodOdTemp);
									pocetniDatumOdabira.add(Calendar.DATE, -1);

									Integer mjesec = pocetniDatumOdabira.get(Calendar.MONTH)+1;
									String mjesString = mjesec.toString();
									Integer dan = pocetniDatumOdabira.get(Calendar.DATE);
									String danString = null;

									if(dan<=9)
										{
											danString = "0"+dan.toString();
										}
									else
										{
										danString = dan.toString();
										}

									Integer godina = pocetniDatumOdabira.get(Calendar.YEAR);
									String godinaString = godina.toString();

									String pocetniDanOdabira = godinaString+"-"+mjesString+"-"+danString;

									java.sql.Date sqlPeriodOd = java.sql.Date.valueOf(transDataPeriodOd);
									java.sql.Date sqlPeriodDo = java.sql.Date.valueOf(transDataPeriodDo);

									if(compareData(sqlPeriodOd, sqlPeriodDo, userData))
										{
										log.debug("MAP CODE > -- sqlSnippetIzvadakEvidRadSatSelect izmjena- dohvat kalendara rada za odabranog zaposlenika (retrieve calendar of work for selected employees)");
											//lzaposlenikRadniKalendar = sqlSnippetIzvadakEvidRadSatSelect(trx,rs,parm,sqlPeriodOd,sqlPeriodDo,IDzaposlenikUnos, userData);
											lzaposlenikRadniKalendarInt = sqlSnippetIzvadakEvidRadSatSelect(trx,rs,parm,sqlPeriodOd,sqlPeriodDo,IDzaposlenikUnos, userData);
											log.debug("MAP CODE > -- sqlSnippetIzvadakEvidRadSatSelect izmjena - end");
											//userData.put(TRANS_LIST_ZAPOSLENIK_RADNI_KALENDAR, lzaposlenikRadniKalendar);



												System.out.println("------------------------------------------"+ lzaposlenikRadniKalendarInt.get(1).getVr24());


											userData.put(TRANS_LIST_ZAPOSLENIK_RADNI_KALENDAR, lzaposlenikRadniKalendarInt);
										}
									else
										{

											prikazTablice = "losUnos";
											userData.put(TRANS_TABLICA_EVID_VIEW, prikazTablice);
											/*if(checkDataValue)
											{
											userData.put(TRANS_LOS_UNOS_MSG, "Datum unesen u polje 'Od' je veÄ‡i od datuma unesenog u polje 'Do'");
											System.out.println("VALUE JE TRUE - " +checkDataValue);
											}
											else
											{
												userData.put(TRANS_LOS_UNOS_MSG, "Forma za unos vremenskog perioda nije popunjena !");
												System.out.println("VALUE JE FALSE - " +checkDataValue);
											}*/

											//userData.put(TRANS_LOS_UNOS_MSG, "Krivi unos unutar forme vremenski 'period'");
											clearTransientBadEnter(userData);

										}
								}
							catch (Exception e)
								{
									// TODO: handle exception
								}
							finally
								{
									if (trx != null)
										trx.closeIfPossible();
								}

							userData.put(TRANS_PERIOD_OD, periodOd);
							userData.put(TRANS_PERIOD_DO, periodDo);




					}

			}

			else if (COMMAND_REFERENT_ISPIS.equals(command))
			{
				if(userData.get(TRANS_BROJZAPOSLENIKA).toString().length()==0)
					{
						clearTransientBadEnter(userData);
					}
				else
					{
						Boolean checkDataValue = true;
						prikazTablice = "ispis";
						userData.put(TRANS_TABLICA_EVID_VIEW, prikazTablice);

						if(periodDo.length() == 0)
							{
								periodDo=periodOd;
							}
						else{}
						if (periodOd.length() == 0)
							{
								periodOd=periodDo;
							}
						else{}
						if(periodOd.length() == 0 || periodDo.length() == 0)
							{
								periodOd = "02.11.2010";
								periodDo = "01.11.2010";
								//checkDataValue = false;
							}
						else{}

						String IDzaposlenikUnos = userData.get(TRANS_BROJZAPOSLENIKA).toString(); // dohvatiti id zaposlenika ulogiranog u aplikaciju
						trx = ctxA.getDatabaseManager().createTransaction();

						try
							{
								String transDataPeriodOd = periodOd.substring(6, 10)+"-"+periodOd.substring(3, 5)+"-"+periodOd.substring(0,2);
								String transDataPeriodDo = periodDo.substring(6, 10)+"-"+periodDo.substring(3, 5)+"-"+periodDo.substring(0,2);

								//System.out.println("OD : "+transDataPeriodOd+" DO :"+transDataPeriodDo);

								java.sql.Date sqlPeriodOd = java.sql.Date.valueOf(transDataPeriodOd );
								java.sql.Date sqlPeriodDo = java.sql.Date.valueOf(transDataPeriodDo);

								if(compareData(sqlPeriodOd, sqlPeriodDo, userData))
									{
										log.debug("MAP CODE > -- sqlSnippetIzvadakEvidRadSatSelect ispis- dohvat kalendara rada za odabranog zaposlenika (retrieve calendar of work for selected employees)");
										//lzaposlenikRadniKalendar = sqlSnippetIzvadakEvidRadSatSelect(trx,rs,parm,sqlPeriodOd,sqlPeriodDo,IDzaposlenikUnos,userData);
										lzaposlenikRadniKalendarInt = sqlSnippetIzvadakEvidRadSatSelect(trx,rs,parm,sqlPeriodOd,sqlPeriodDo,IDzaposlenikUnos,userData);
										log.debug("MAP CODE > -- sqlSnippetIzvadakEvidRadSatSelect ispis- end");
										//System.out.println("- - - - - "+lzaposlenikRadniKalendar);
										//userData.put(TRANS_LIST_ZAPOSLENIK_RADNI_KALENDAR, lzaposlenikRadniKalendar);
										System.out.println("- - - - - "+lzaposlenikRadniKalendarInt);
										userData.put(TRANS_LIST_ZAPOSLENIK_RADNI_KALENDAR, lzaposlenikRadniKalendarInt);
									}
								else
									{
										prikazTablice = "losUnos";
										userData.put(TRANS_TABLICA_EVID_VIEW, prikazTablice);
										//userData.put(TRANS_LOS_UNOS_MSG, "Forma za unos vremenskog perioda je krivo popunjena !");
										//userData.put(TRANS_LOS_UNOS_MSG, "Krivi unos unutar forme vremenski period");
										/*if(checkDataValue)
										{
										userData.put(TRANS_LOS_UNOS_MSG, "Datum unesen u polje 'Od' ("+periodOd+") je veÄ‡i od datuma unesenog u polje 'Do' ("+periodDo+")");
										}
										else
										{
											userData.put(TRANS_LOS_UNOS_MSG, "Forma za unos vremenskog perioda nije popunjena !");
										}*/

										clearTransientBadEnter(userData);
									}

							}
						catch (Exception e)
							{
								System.out.println("COMMAND ISPIS ERROR : "+e.getMessage());
							}
						finally
							{
								if (trx != null)
									trx.closeIfPossible();
							}

						userData.put(TRANS_PERIOD_OD, periodOd);
						userData.put(TRANS_PERIOD_DO, periodDo);
					}
				}


			else if (COMMAND_EVID_ZAPOSLENIK_SELECT.equals(command)) {
				System.out.println("COMMAND_EVID_ZAPOSLENIK_SELECT");
				String dataDumpSumarno = null;
				String dataDumpAnalitika = null;
				String dataRadniKalendar0101 = "";
				String dataRadniKalendar0102 = "";
				int dataRadniKalendar02int = 0;
				String dataRadniKalendar02 = "";
				String dataRadniKalendar0301 = "";
				String dataRadniKalendar0701 = "";
				String dataRadniKalendar08 = "";
				String dataRadniKalendar09 = "";
				String dataRadniKalendar11 = "";
				String dataRadniKalendar13 = "";
				String dataRadniKalendar20 = "";
				String dataRadniKalendar23 = "";
				String dataRadniKalendar25 = "";
				String dataRadniKalendar26 = "";
				String dataRadniKalendar37 = "";
				String dataRadniKalendar38 = "";
				int dataRadniKalendar38int = 0;
				String dataRadniKalendar40 = "";
				String dataRadniKalendar41 = "";
				String dataRadniKalendar42 = "";
				String dataRadniKalendar420 = "";
				String dataRadniKalendar421 = "";
				System.out.println("KKKKKKKKKKKKKKKKKKKKK "+mjesecObracuna);
				BigDecimal raz = new BigDecimal(0.21);
				BigDecimal bd1 = new BigDecimal(8.00);

				zaposlenikSelectSnippet(userData, brojZaposlenika, lZaposlenik, mjesecObracuna);
				//-------------TO DO - dohvat podataka iz radnog kalendara--------------
				if(mjesecObracuna!=null)
				{
				String godinaObracunskogPerioda = mjesecObracuna.substring(0, 4);
				String  mjesecObracunskiPeriod =  mjesecObracuna.substring(4,6);

				//parm.add(brojZaposlenika);
				parm.addAll(java.util.Arrays.asList(new Object[] { brojZaposlenika, godinaObracunskogPerioda, mjesecObracunskiPeriod }));
				rs = (ResultSet) trx.executePreparedQuerySnapShotById(moduleName, "radniKalendarSelect", parm);
				System.out.println(" -------------- "+godinaObracunskogPerioda+" -- "+mjesecObracunskiPeriod);
				int k = 1;
				int zadnjiDatum = 0;

				if(rs.next())
				{
					zadnjiDatum = Integer.parseInt(rs.getDate("KalendarZapisaDatumUnosa").toString().substring(8));
				while(rs.next())
				{
					System.out.println("USLO U rs.next");
					zadnjiDatum = Integer.parseInt(rs.getDate("K" +
							"alendarZapisaDatumUnosa").toString().substring(8));
					System.out.println("zadnji datum u odabiru "+Integer.parseInt(rs.getDate("KalendarZapisaDatumUnosa").toString().substring(8)));
				}
				System.out.println("zadnji datum u odabiru "+zadnjiDatum);
				for(int i = 0;i<zadnjiDatum;i++)
				{
					System.out.println(k+" -- "+i+" --"+rs.absolute(k)+" -- "+Integer.parseInt(rs.getDate("KalendarZapisaDatumUnosa").toString().substring(8)));
					if(Integer.parseInt(rs.getDate("KalendarZapisaDatumUnosa").toString().substring(8))== i+1)
					{
						//System.out.println(" ---zzzzz -- "+raz.add(rs.getBigDecimal("vr06")).setScale(2,BigDecimal.ROUND_HALF_EVEN).toBigInteger());
					dataRadniKalendar0101 = dataRadniKalendar0101+raz.add(rs.getBigDecimal("vr06")).setScale(0,BigDecimal.ROUND_HALF_EVEN).toBigInteger().toString()+"/";
					dataRadniKalendar0102 = dataRadniKalendar0102+raz.add(rs.getBigDecimal("vr07")).setScale(0,BigDecimal.ROUND_HALF_EVEN).toBigInteger().toString()+"/";
					dataRadniKalendar02int = dataRadniKalendar02int + raz.add(rs.getBigDecimal("vr14")).setScale(0,BigDecimal.ROUND_HALF_EVEN).toBigInteger().intValue();
					dataRadniKalendar02 = Integer.toString(dataRadniKalendar02int);
					dataRadniKalendar0301 = dataRadniKalendar0301+raz.add(rs.getBigDecimal("vr15")).setScale(0,BigDecimal.ROUND_HALF_EVEN).toBigInteger().toString()+"/";
					dataRadniKalendar0701 = dataRadniKalendar0701+rs.getBigDecimal("vr16").toBigInteger().toString()+"/";
					dataRadniKalendar08 = dataRadniKalendar08+rs.getBigDecimal("vr18").toBigInteger().toString()+"/";
					dataRadniKalendar09 = dataRadniKalendar09+rs.getBigDecimal("vr23").toBigInteger().toString()+"/";
					dataRadniKalendar11 = dataRadniKalendar11+raz.add(rs.getBigDecimal("vr12")).setScale(0,BigDecimal.ROUND_HALF_EVEN).toBigInteger().toString()+"/";
					dataRadniKalendar13 = dataRadniKalendar13+rs.getBigDecimal("vr25").toBigInteger().toString()+"/";
					dataRadniKalendar20 = dataRadniKalendar20+rs.getBigDecimal("vr17").toBigInteger().toString()+"/";
					dataRadniKalendar23 = dataRadniKalendar23+rs.getBigDecimal("vr22").toBigInteger().toString()+"/";
					System.out.println(" -- "+bd1.subtract(raz.add(rs.getBigDecimal("vr06")).setScale(0,BigDecimal.ROUND_HALF_EVEN)).toBigInteger() .intValue()+" -- "+raz.add(rs.getBigDecimal("vr10")).setScale(0,BigDecimal.ROUND_HALF_EVEN).toBigInteger().intValue());
					if(rs.getBigDecimal("vr10").toBigInteger().intValue()>0)
					{
						if(bd1.subtract(raz.add(rs.getBigDecimal("vr06")).setScale(0,BigDecimal.ROUND_HALF_EVEN)).toBigInteger() .intValue()> raz.add(rs.getBigDecimal("vr10")).setScale(0,BigDecimal.ROUND_HALF_EVEN).toBigInteger().intValue())
						{
							dataRadniKalendar25 = dataRadniKalendar25+raz.add(rs.getBigDecimal("vr10")).setScale(0,BigDecimal.ROUND_HALF_EVEN).toBigInteger().toString()+"/";
						}
						else{
							dataRadniKalendar25 = dataRadniKalendar25+raz.add(rs.getBigDecimal("vr10")).setScale(0,BigDecimal.ROUND_DOWN).toBigInteger().toString()+"/";

						}
					}
					else
					{

							if(rs.getBigDecimal("vr26").toBigInteger().intValue()>0)
							{
								dataRadniKalendar25 = dataRadniKalendar25+rs.getBigDecimal("vr26").toBigInteger().toString()+"/";
							}
							else{dataRadniKalendar25 = dataRadniKalendar25+"0/";}

					}

					if(rs.getBigDecimal("vr11").toBigInteger().intValue()>0)
					{
						if(bd1.subtract(raz.add(rs.getBigDecimal("vr06")).setScale(0,BigDecimal.ROUND_HALF_EVEN)).toBigInteger() .intValue()> raz.add(rs.getBigDecimal("vr11")).setScale(0,BigDecimal.ROUND_HALF_EVEN).toBigInteger().intValue())
						{
							dataRadniKalendar26 = dataRadniKalendar26+raz.add(rs.getBigDecimal("vr11")).setScale(0,BigDecimal.ROUND_HALF_EVEN).toBigInteger().toString()+"/";
						}
						else
						{
							dataRadniKalendar26 = dataRadniKalendar26+raz.add(rs.getBigDecimal("vr11")).setScale(0,BigDecimal.ROUND_DOWN).toBigInteger().toString()+"/";
						}
						}
					else{
						if(rs.getBigDecimal("vr27").toBigInteger().intValue()>0)
						{
							dataRadniKalendar26 = dataRadniKalendar26+rs.getBigDecimal("vr27").toBigInteger().toString()+"/";
						}
						else if (rs.getBigDecimal("vr24").toBigInteger().intValue()>0)
						{
							dataRadniKalendar26 = dataRadniKalendar26+rs.getBigDecimal("vr24").toBigInteger().toString()+"/";
						}
						else {dataRadniKalendar26 = dataRadniKalendar26+"0/";}
					}

					dataRadniKalendar37 = dataRadniKalendar37+rs.getString("vr20")+"/";

					System.out.println("ODVOJENI ZICOT : "+rs.getString("vr47"));

					if(rs.getString("vr47") != null && rs.getString("vr47").equals(""))
					{
						System.out.println(" nnnnnnnnnnnnnnnnnnnnnn ");
						dataRadniKalendar38int = dataRadniKalendar38int + 0;
					}
					else if(rs.getString("vr47") == null){
						System.out.println("ODVOKENI Ĺ˝IVOT JE NULL");
						dataRadniKalendar38int = dataRadniKalendar38int + 0;
					}
					else{
						System.out.println("ELSE ODVOJENI ZIVOT");
						dataRadniKalendar38int = dataRadniKalendar38int + Integer.parseInt(rs.getString("vr47"));
					}

					dataRadniKalendar38 = Integer.toString(dataRadniKalendar38int);

					if(rs.getBigDecimal("vr19").toBigInteger().toString().equals("16"))
					{
						System.out.println(rs.getString(3));
						dataRadniKalendar40 = dataRadniKalendar40+rs.getBigDecimal("vr19").toBigInteger().toString()+"/";
					}
					else {
						dataRadniKalendar40 = dataRadniKalendar40+"0"+"/";
					}
					if(rs.getBigDecimal("vr19").toBigInteger().toString().equals("24"))
					{
						System.out.println(rs.getString(3));
						dataRadniKalendar41 = dataRadniKalendar41+rs.getBigDecimal("vr19").toBigInteger().toString()+"/";
					}
					else {
						dataRadniKalendar41 = dataRadniKalendar41+"0"+"/";
					}
					//----------------------------
					if(rs.getBigDecimal("vr21").toBigInteger().toString().equals("16"))
					{
						System.out.println(rs.getString(3));
						dataRadniKalendar420 = dataRadniKalendar420+rs.getBigDecimal("vr21").toBigInteger().toString()+"/";
					}
					else {
						dataRadniKalendar420 = dataRadniKalendar420+"0"+"/";
					}
					if(rs.getBigDecimal("vr21").toBigInteger().toString().equals("24"))
					{
						System.out.println(rs.getString(3));
						dataRadniKalendar421 = dataRadniKalendar421+rs.getBigDecimal("vr21").toBigInteger().toString()+"/";
					}
					else {
						dataRadniKalendar421 = dataRadniKalendar421+"0"+"/";
					}

					//dataRadniKalendar420 = dataRadniKalendar420+rs.getBigDecimal("vr21").toBigInteger().toString()+"/";
					//dataRadniKalendar421 = dataRadniKalendar421+rs.getBigDecimal("vr48").toBigInteger().toString()+"/";

					k++;
					}
					else
					{
						dataRadniKalendar0101 = dataRadniKalendar0101+"0/";
						dataRadniKalendar0102 = dataRadniKalendar0102+"0/";
						dataRadniKalendar0301 = dataRadniKalendar0301+"0/";
						dataRadniKalendar0701 = dataRadniKalendar0701+"0/";
						dataRadniKalendar08 = dataRadniKalendar08+"0/";
						dataRadniKalendar09 = dataRadniKalendar09+"0/";
						dataRadniKalendar11 = dataRadniKalendar11+"0/";
						dataRadniKalendar13 = dataRadniKalendar13+"0/";
						dataRadniKalendar20 = dataRadniKalendar20+"0/";
						dataRadniKalendar23 = dataRadniKalendar23+"0/";
						dataRadniKalendar25 = dataRadniKalendar25+"0/";
						dataRadniKalendar26 = dataRadniKalendar26+"0/";
						dataRadniKalendar37 = dataRadniKalendar37+"0/";
						dataRadniKalendar40 = dataRadniKalendar40+"0/";
						dataRadniKalendar41 = dataRadniKalendar41+"0/";
						dataRadniKalendar420 = dataRadniKalendar420+"0/";
						dataRadniKalendar421 = dataRadniKalendar421+"0/";
					}

				}


				while(rs.next())
				{
					System.out.println("------- RRRRRRRRRRRRRoWS: "+rs.getRow());
					System.out.println(Integer.parseInt(rs.getDate("KalendarZapisaDatumUnosa").toString().substring(8)));
					//dataRadniKalendar0101 = dataRadniKalendar0101+rs.getBigDecimal("vr06").toBigInteger().toString()+"/";

					dataRadniKalendar0301 = dataRadniKalendar0301+rs.getBigDecimal("vr15").toBigInteger().toString()+"/";
					dataRadniKalendar0701 = dataRadniKalendar0701+rs.getBigDecimal("vr16").toBigInteger().toString()+"/";
					dataRadniKalendar08 = dataRadniKalendar08+rs.getBigDecimal("vr17").toBigInteger().toString()+"/";
					dataRadniKalendar09 = dataRadniKalendar09+rs.getBigDecimal("vr23").toBigInteger().toString()+"/";
					dataRadniKalendar11 = dataRadniKalendar11+rs.getBigDecimal("vr12").toBigInteger().toString()+"/";
					dataRadniKalendar13 = dataRadniKalendar13+rs.getBigDecimal("vr25").toBigInteger().toString()+"/";


					if(rs.getBigDecimal("vr19").toBigInteger().toString().equals("16"))
					{
						System.out.println(rs.getString(3));
						dataRadniKalendar40 = dataRadniKalendar40+rs.getBigDecimal("vr19").toBigInteger().toString()+"/";
					}
					else {
						dataRadniKalendar40 = dataRadniKalendar40+"0"+"/";
					}
					if(rs.getBigDecimal("vr19").toBigInteger().toString().equals("24"))
					{
						System.out.println(rs.getString(3));
						dataRadniKalendar41 = dataRadniKalendar41+rs.getBigDecimal("vr19").toBigInteger().toString()+"/";
					}
					else {
						dataRadniKalendar41 = dataRadniKalendar41+"0"+"/";
					}
					//---------------------------

					if(rs.getBigDecimal("vr21").toBigInteger().toString().equals("16"))
					{
						System.out.println(rs.getString(3));
						dataRadniKalendar420 = dataRadniKalendar420+rs.getBigDecimal("vr21").toBigInteger().toString()+"/";
					}
					else {
						dataRadniKalendar420 = dataRadniKalendar420+"0"+"/";
					}
					if(rs.getBigDecimal("vr21").toBigInteger().toString().equals("24"))
					{
						System.out.println(rs.getString(3));
						dataRadniKalendar421 = dataRadniKalendar421+rs.getBigDecimal("vr21").toBigInteger().toString()+"/";
					}
					else {
						dataRadniKalendar41 = dataRadniKalendar41+"0"+"/";
					}
					//dataRadniKalendar420 = dataRadniKalendar420+rs.getBigDecimal("vr21").toBigInteger().toString()+"/";
					//dataRadniKalendar421 = dataRadniKalendar421+rs.getBigDecimal("vr48").toBigInteger().toString()+"/";
				}
				}
				else{
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_0101);
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_0102);
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_02);
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_0301);
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_0701);
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_08);
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_09);
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_11);
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_13);
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_20);
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_23);
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_25);
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_26);
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_37);
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_38);
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_40);
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_41);
					//userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_42);
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_420);
					userData.remove(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_421);
				}
				//System.out.println(":: "+dataRadniKalendar);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_0101, dataRadniKalendar0101);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_0102, dataRadniKalendar0102);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_02, dataRadniKalendar02);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_0301, dataRadniKalendar0301);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_0701, dataRadniKalendar0701);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_08, dataRadniKalendar08);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_09, dataRadniKalendar09);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_11, dataRadniKalendar11);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_13, dataRadniKalendar13);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_20, dataRadniKalendar20);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_23, dataRadniKalendar23);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_25, dataRadniKalendar25);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_26, dataRadniKalendar26);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_37, dataRadniKalendar37);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_38, dataRadniKalendar38);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_40, dataRadniKalendar40);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_41, dataRadniKalendar41);
				//userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_42, dataRadniKalendar42);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_420, dataRadniKalendar420);
				userData.put(TRANS_PODACI_ZA_ZAPOSLENIKA_RK_421, dataRadniKalendar421);
				System.out.println(" OOOOOOOOOOOOPPPPPPPPPAAAAAAAAAAAA --- "+dataRadniKalendar0101);
				System.out.println(" OOOOOOOOOOOO   26   AAAAAAAAAAAA --- "+dataRadniKalendar26);
				System.out.println(" OOOOOOOOOOOO   25   AAAAAAAAAAAA --- "+dataRadniKalendar25);
				System.out.println(" OOOOOOOOOOOOPPPPPPPPPAAAAAAAAAAAA --- "+dataRadniKalendar41);
				System.out.println(" OOOOOOOOOOOOPPPPPPPPPAAAAAAAAAAAA --- "+dataRadniKalendar42);
				}
				//-------------------------------------------------------------------------------

				System.out.println("-- UGOVOR ZAPOSLENIKA ="+ugovorZaposlenika);
				userData.put(TRANS_UGOVOR_ZAPOSLENIKA, ugovorZaposlenika);
				parm.clear();
				parm.addAll(java.util.Arrays.asList(new Object[] { brojZaposlenika, mjesecObracuna, obracunskiPeriod }));
				if(ugovorZaposlenika.equals("1"))
				{
					rs = trx.executePreparedQueryById(moduleName, "prisutnostZaposlenikDataDumpSelect", parm);
					if (rs.next()) {
						podaciUBazi = "1";
						dataDumpAnalitika = deserializeDataDumpAnalitikaSnippet(ctxS, rs, userData);
						userData.put(TRANS_DATADUMPANALITIKA, dataDumpAnalitika);
						dataDumpSumarno = deserializeDataDumpSumarnoSnippet(ctxS, rs, userData, null);

						System.out.println("DAZA DUMP SUMARNO : "+dataDumpSumarno);
						userData.put(TRANS_DATADUMPSUMARNO, dataDumpSumarno);
					}
					rs.close();
					userData.put(TRANS_ZAP_UGOVOR, "1");
				}
				else{
					System.out.println("UGOVOR ZAPOSLENIKA NIJE DOPUSTEN");
					userData.put(TRANS_ZAP_UGOVOR, "0");
				}
				userData.put(TRANS_PODACIUBAZI, podaciUBazi);
			} else if (COMMAND_POVIJEST_ZAPOSLENIK_SELECT.equals(command)) {
				List<String> lDataDumpAnalitika = new ArrayList<String>();
				List<String> lDataDumpSumarno = new ArrayList<String>();
				List<PrisutnostZaposlenikPovijest> lPrisutnostZaposlenikPovijest = new ArrayList<PrisutnostZaposlenikPovijest>();
				String dataDumpAnalitika = null;
				String dataDumpSumarno = null;
				zaposlenikSelectSnippet(userData, brojZaposlenika, lZaposlenik, mjesecObracunaHistory);
				parm.clear();
				parm.addAll(java.util.Arrays.asList(new Object[] { brojZaposlenika, mjesecObracunaHistory,
						obracunskiPeriodHistory }));
				rs = trx.executePreparedQueryById(moduleName, "prisutnostZaposlenikPovijestSelect", parm);
				while (rs.next()) {
					fillPrisutnostZaposlenikPovijestSnippet(rs, lPrisutnostZaposlenikPovijest);
					dataDumpAnalitika = deserializeDataDumpAnalitikaSnippet(ctxS, rs, userData);
					lDataDumpAnalitika.add(dataDumpAnalitika == null ? "" : dataDumpAnalitika);
					dataDumpSumarno = deserializeDataDumpSumarnoSnippet(ctxS, rs, userData, null);
					lDataDumpSumarno.add(dataDumpSumarno == null ? "" : dataDumpSumarno);
				}
				rs.close();
				userData.put(TRANS_LIST_DATADUMPANALITIKA, lDataDumpAnalitika);
				userData.put(TRANS_LIST_DATADUMPSUMARNO, lDataDumpSumarno);
				userData.put(TRANS_LIST_PRISUTNOST_ZAPOSLENIK_POVIJEST, lPrisutnostZaposlenikPovijest);
				userData.put(TRANS_PODACIUBAZI, 1);
			} else if (COMMAND_RUKOVODITELJ_OJ_SELECT.equals(command)) {
				lZaposlenik = new ArrayList<Zaposlenik>();
				String dataDumpSumarno = null;
				List<String> lDataDumpSumarno = new ArrayList<String>();
				Map<String, String> mPrisutnostUnosnikEmail = (Map) userData.get(TRANS_MAP_PRISUTNOST_UNOSNIK_EMAIL);
				if (mPrisutnostUnosnikEmail == null)
					mPrisutnostUnosnikEmail = new HashMap<String, String>();
				String unosnikUsername = null;
				String unosnikEmail = null;
				int status = -1;
				parm.clear();
				parm.addAll(Arrays.asList(datumDohvata, datumDohvata, datumDohvata, datumDohvata, datumDohvata,
						datumDohvata, datumDohvata, mjesecObracuna, obracunskiPeriod, sifraOJ, datumDohvata, datumDohvata,
						datumDohvata, datumDohvata, datumDohvata, datumDohvata, datumDohvata, datumDohvata, datumDohvata,
						datumDohvata, datumDohvata, datumDohvata));
				rs = (ResultSet) trx.executePreparedQueryById(moduleName, "radniciUOJDataDumpSelect", parm);
				while (rs.next()) {
					zaposlenik = fillZaposlenikSnippet(zaposlenik, rs, lZaposlenik);
					status = zaposlenik.getPrisutnostStatus();

					unosnikUsername = rs.getString("PrisutnostPotvrdio");
					if (unosnikUsername != null && !mPrisutnostUnosnikEmail.containsKey(unosnikUsername) && status == 1) {
						try {
							unosnikEmail = UtilEvidencijaRada.getPumaEmailFromUsername(unosnikUsername);
						} catch (IllegalArgumentException e) {
							unosnikEmail = null;
							log.error("Nije moguce iz LDAP-a dohvatiti podatke o unosniku " + unosnikUsername
									+ MSG_GUTENBERG);
						}
						mPrisutnostUnosnikEmail.put(unosnikUsername, unosnikEmail);
					}

					dataDumpSumarno = deserializeDataDumpSumarnoSnippet(ctxS, rs, userData, zaposlenik.getBrojZaposlenika());
					lDataDumpSumarno.add(dataDumpSumarno == null ? "" : dataDumpSumarno);
				}
				userData.put(TRANS_LIST_DATADUMPSUMARNO, lDataDumpSumarno);
				userData.put(TRANS_LIST_ZAPOSLENIK, lZaposlenik);
				userData.put(TRANS_MAP_PRISUTNOST_UNOSNIK_EMAIL, mPrisutnostUnosnikEmail);
			} else if (COMMAND_MENU_PRIJEPIS.equals(commandNavigation)) {
				StatusPrisutnostiPoSektorima statusPrisutnostiPoSektorima = null;
				StatusPrisutnostiPoSektorima beanDummyStatusPrisutnostiPoSektorima = new StatusPrisutnostiPoSektorima();

				List<StatusPrisutnostiPoSektorima> lStatusPrisutnostiPoSektorima = new ArrayList<StatusPrisutnostiPoSektorima>();
				parm.clear();
				parm.addAll(java.util.Arrays.asList(new Object[] { datumDohvata, mjesecObracuna, obracunskiPeriod,
						datumDohvata, datumDohvata, datumDohvata, datumDohvata, datumDohvata, datumDohvata }));
				rs = trx.executePreparedQuerySnapShotById(moduleName, "statusPrisutnostiPoSektorimaSelect", parm);
				lStatusPrisutnostiPoSektorima = beanDataHandler.getBeansFromResultSet(beanDummyStatusPrisutnostiPoSektorima,
						rs);
				userData.put(TRANS_LIST_STATUS_PRISUTNOSTI_PO_SEKTORIMA, lStatusPrisutnostiPoSektorima);

				StatusPrisutnostiSkupno statusPrisutnostiSkupno = new StatusPrisutnostiSkupno();
				StatusPrisutnostiSkupno beanDummyStatusPrisutnostiSkupno = new StatusPrisutnostiSkupno();
				parm.clear();
				parm.addAll(java.util.Arrays.asList(new Object[] { datumDohvata, mjesecObracuna, obracunskiPeriod,
						datumDohvata, datumDohvata, datumDohvata, datumDohvata, datumDohvata, datumDohvata }));
				rs = trx.executePreparedQuerySnapShotById(moduleName, "statusPrisutnostiSkupnoSelect", parm);
				if (rs.next())
					statusPrisutnostiSkupno = beanDataHandler.getBeanFromResultSet(beanDummyStatusPrisutnostiSkupno, rs);
				userData.put(TRANS_STATUS_PRISUTNOSTI_SKUPNO, statusPrisutnostiSkupno);
			} else if (COMMAND_PRIJEPIS_IZVJESTAJ.equals(command)) {
				if (mjesecObracunaHistory == null || obracunskiPeriodHistory == null)
					throw new BusinessException("Nije odabran mjesec obraÄŤuna.");
				RowSet rs2 = null;
				parm.clear();
				parm.addAll(java.util.Arrays.asList(new Object[] { mjesecObracunaHistory, obracunskiPeriodHistory }));
				List<Object> lSPRet = trx.executeStoredProcedureById(moduleName, "spPrisutnostIzvjestajPivot", parm);
				rs = (RowSet) lSPRet.get(0);
				String reportName1 = "Prisutnost_" + mjesecObracunaHistory;
				String reportPath = ctxA.getFileManager().getTemporaryFolderPath()
						+ ctxA.getFileManager().getFileSeparator() + reportName1 + "_" + (new Date().getTime()) + ".xls";
				parm.clear();
				parm.addAll(java.util.Arrays.asList(new Object[] { datumDohvataHistory, datumDohvataHistory,
						datumDohvataHistory, datumDohvataHistory, datumDohvataHistory, datumDohvataHistory,
						datumDohvataHistory, datumDohvataHistory, "%", mjesecObracunaHistory.substring(0, 4),
						mjesecObracunaHistory.substring(4), obracunskiPeriodHistory }));
				rs2 = trx.executePreparedQuerySnapShotById(moduleName, "prisutnostIzvjestajNoPivot", parm);
				String reportName2 = "Prisutnost_" + mjesecObracunaHistory + "_nopivot";
				ctxA.getReportManager()
						.createExcelReport(reportPath, Arrays.asList(new String[] { reportName1, reportName2 }),
								Arrays.asList(new ResultSet[] { rs, rs2 }));
				//sendByteStreamRenderResponse(response, reportPath, reportName);
				request.getPortletSession().setAttribute(Const.TRANS_DOWNLOAD_PATH, reportPath,
						PortletSession.APPLICATION_SCOPE);
				request.getPortletSession().setAttribute(Const.TRANS_DOWNLOAD_NAME, reportName1 + ".xls",
						PortletSession.APPLICATION_SCOPE);
				String url = request.getContextPath() + "/DownloadServlet"; //"/jsp/Download.jsp"
				url = response.encodeURL(url);
				response.sendRedirect(url);
			}
		} catch (Exception e) {
			UtilConsole.printStackTrace(e);
		}
		finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					UtilConsole.printStackTrace(e);
				}
			}
			if (trx != null)
				trx.closeIfPossible();
		}
	}

private String deserializeDataDumpAnalitikaSnippet(ISessionContext ctxS, ResultSet rs, Map<String, Object> userData) throws SQLException, IOException {
			String dataDumpAnalitika = null;
				Blob dataDumpBlob;
				byte[] barr;
				dataDumpBlob = rs.getBlob("DataDumpAnalitika");
				barr = UtilTypeConversion.blobToByteArray(dataDumpBlob);
				try {
					dataDumpAnalitika = (String) UtilSerialization.deserialize(barr);
					}
				catch (Exception e)
				{
					ctxS.getLogManager().debug("Problem prilikom deserijalizacije DataDumpAnalitika. Reason:" + e.getMessage());
					UtilConsole.printStackTrace(e);
				}
				return dataDumpAnalitika;
	}

private String deserializeDataDumpSumarnoSnippet(ISessionContext ctxS, ResultSet rs, Map<String, Object> userData,
		String brojZaposlenika) throws SQLException, IOException {
	Blob dataDumpBlob;
	HashMap<String, Object> mDataDumpSumarno = null;
	byte[] barr;
	dataDumpBlob = rs.getBlob("DataDumpSumarno");
	barr = UtilTypeConversion.blobToByteArray(dataDumpBlob);
	try {
		if (barr != null) {
			mDataDumpSumarno = (HashMap) UtilSerialization.deserialize(barr);
			//ctxS.getLogManager().debug("dataDumpSumarno: " + mDataDumpSumarno.toString());
		}
	} catch (Exception e) {
		ctxS.getLogManager().debug(
				"Problem prilikom deserijalizacije DataDumpSumarno."
						+ (brojZaposlenika == null ? "" : " Broj zaposlenika: " + brojZaposlenika) + " Reason:"
						+ e.getMessage());
		UtilConsole.printStackTrace(e);
	}

	return barr == null ? null : mDataDumpSumarno.toString();
}

private void zaposlenikSelectSnippet(Map<String, Object> userData, String brojZaposlenika, List<Zaposlenik> lZaposlenik,
		String mjesecObracuna) {
	System.out.println("//////////////////////"+mjesecObracuna);
	String imePrezime = null;
	int statusPrisutnostiZaposlenika = -1;
	Zaposlenik zaposlenikBean = (Zaposlenik) userData.get(TRANS_BEAN_ZAPOSLENIK);
	if (brojZaposlenika != null) {
		for (Zaposlenik zap : lZaposlenik) {
			if (brojZaposlenika.equals(zap.getBrojZaposlenika())) {
				zaposlenikBean = zap;
				imePrezime = zap.getPrezimeImeZap();
				statusPrisutnostiZaposlenika = zap.getPrisutnostStatus();
				if(mjesecObracuna!=null)
				{
				updateZaposlenikDanUnosaPrviZadnji(zaposlenikBean, userData, mjesecObracuna);
				break;
				}
			}
		}
	}
	userData.put(TRANS_IMEPREZIME, imePrezime);
	userData.put(TRANS_BROJZAPOSLENIKA, brojZaposlenika);
	userData.put(TRANS_STATUSPRISUTNOSTIZAPOSLENIKA, statusPrisutnostiZaposlenika);
	userData.put(TRANS_TERENSKIRADNIK, zaposlenikBean.getTerenskiRadnik());
	userData.put(TRANS_BEAN_ZAPOSLENIK, zaposlenikBean);
}

private void initData(ISessionContext ctxS) throws BusinessException {
	ITransaction trx = null;
	ResultSet rs = null;
	List<Object> parm = new ArrayList<Object>();
	ILogManager log = null;
	List<String> lUserOJs = null;
	List<String> lUserFunctions = null;
	List<String> lVrsteRadaKludge = new ArrayList<String>();
	List<OrgJedinica> lOJ = new ArrayList<OrgJedinica>();
	StatusPrisutnost obracunURadu = null;
	StatusPrisutnost obracunUPripremi = null;
	StatusPrisutnost obracunSvi = null;
	List<StatusPrisutnost> lObracunURadu = new ArrayList<StatusPrisutnost>();
	List<StatusPrisutnost> lObracunUPripremi = new ArrayList<StatusPrisutnost>();
	List<StatusPrisutnost> lObracunSvi = new ArrayList<StatusPrisutnost>();
	String LoginPoduzece = null;

	List<VrstaRada> lVrstaRada = new ArrayList<VrstaRada>();

	Integer daysInMonth = 0;
	Integer year = 0;
	Integer month = 0;
	String username = null;
	String mjesecObracuna = null;
	Integer obracunskiPeriod = null;
	String tempMO = null;
	try {
		Map<String, Object> userData = UtilEvidencijaRada.getUserData(ctxS);
		log = ctxS.getLogManager();

		lUserFunctions = UtilEvidencijaRada.getUserFunctions();
		lUserOJs = UtilEvidencijaRada.getUserOJs();
		username = UtilEvidencijaRada.getPumaUserName();
		userData.put(USERNAME, username);
		//log.debug("PumaUserAttribute: " + UtilEvidencija.getPumaUserAttribute(null, "admin"));
		//log.debug("PumaGroupsAll: " + UtilEvidencija.getPumaGroupsAll());
		//log.debug("PumaUserAttributes: ");
		//System.out.println("PumaUserAttributes: ");
		//UtilEvidencija.getPumaUserAttributes();




		log.debug("UserFunctions: " + lUserFunctions);
		log.debug("UserOJs: " + lUserOJs);
		if (lUserOJs.size() == 0 && UtilOther.conditionsEnabled(1))
			lUserOJs.addAll(Arrays.asList(new String[] { "V", "S300", "V600", "SVE" })); //"SVE"
		if (lUserFunctions.size() == 0 && UtilOther.conditionsEnabled(1)) {
			lUserFunctions.addAll(Arrays.asList(new String[] { FJA_EVID03 }));
			Collections.sort(lUserFunctions, String.CASE_INSENSITIVE_ORDER);
		}
		userData.put(TRANS_LIST_USER_FUNCTIONS, lUserFunctions);
		userData.put(TRANS_USER_OJS, lUserOJs);
		lVrsteRadaKludge.addAll(Arrays.asList(new String[] { "VrsteRadM", "Vrste Rada" }));
		userData.put(TRANS_LIST_VRSTE_RADA_KLUDGE, lVrsteRadaKludge);
		java.sql.Date datumDohvata = new java.sql.Date(UtilDate.stripTimeFromDate(new Date()).getTime());
		trx = ctxA.getDatabaseManager().createTransaction();
		parm.clear();
		//parm.add(1);
		parm.add(Arrays.asList(new Integer[] { 1 }));
		rs = (ResultSet) trx.executePreparedQuerySnapShotById(moduleName, "prisutnostStatusSelect", parm);
		if (rs.next()) {
			userData.put(TRANS_OBRACUN_U_RADU_COUNT, 1);
			mjesecObracuna = rs.getString("MjesecObracuna");
			obracunskiPeriod = rs.getInt("ObracunskiPeriod");
			obracunURadu = new StatusPrisutnost();
			obracunURadu.setMjesecObracuna(mjesecObracuna);
			year = UtilEvidencijaRada.getMjesecObracunaGodina(mjesecObracuna);
			month = UtilEvidencijaRada.getMjesecObracunaMjesec(mjesecObracuna);
			daysInMonth = UtilDate.lastDayOfMonthInt(year, month);
			datumDohvata = new java.sql.Date(UtilDate.lastDayOfMonth(year, month).getTime());
			obracunURadu.setDaniObrMjeseca(daysInMonth);
			obracunURadu.setGodina(mjesecObracuna.substring(0, 4));
			obracunURadu.setMjesec(mjesecObracuna.substring(4));
			obracunURadu.setObracunskiPeriod(obracunskiPeriod);
			obracunURadu.setStatus(rs.getInt("Status"));
			lObracunURadu.add(obracunURadu);
			userData.put(TRANS_STATUSPRISUTNOSTI, obracunURadu.getStatus());
			userData.put(TRANS_MJESEC_OBRACUNA, mjesecObracuna);
			userData.put(TRANS_MJESEC_OBRACUNA_GODINA, year);
			userData.put(TRANS_MJESEC_OBRACUNA_MJESEC, month);
			userData.put(TRANS_OBRACUNSKI_PERIOD, obracunskiPeriod);
			userData.put(TRANS_BEAN_STATUS_PRISUTNOSTI, obracunURadu);
			userData.put(TRANS_DAYS_IN_MONTH, daysInMonth);
			userData.put(TRANS_LIST_OBRACUN_U_RADU, lObracunURadu);
			kludgeSnippet(userData, mjesecObracuna, false);
			userData.put(WARN_UPRAVLJANJE_OBRACUN_U_RADU, "ObraÄŤun za " + month + ". mj. " + year
					+ ". g. je u tijeku. MoguÄ‡e je postaviti status U RADU samo za jedan obraÄŤun istovremeno.");
		} else {
			userData.put(TRANS_OBRACUN_U_RADU_COUNT, 0);
			GregorianCalendar calTemp = new GregorianCalendar();
			calTemp.setTime(new Date());
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMinimumIntegerDigits(2);
			String mjesecObracunaT = new Integer(calTemp.get(Calendar.YEAR)).toString()
					+ nf.format(calTemp.get(Calendar.MONTH) + 1);
			calTemp.add(Calendar.MONTH, -1);
			String mjesecObracunaTM1 = new Integer(calTemp.get(Calendar.YEAR)).toString()
					+ nf.format(calTemp.get(Calendar.MONTH) + 1);

			List<String> lMjesecObracunaTemp = Arrays.asList(new String[] { mjesecObracunaT, mjesecObracunaTM1 });
			for (String mjesecObracunaTemp : lMjesecObracunaTemp) {
				obracunUPripremi = new StatusPrisutnost();
				obracunUPripremi.setGodina(mjesecObracunaTemp.substring(0, 4));
				obracunUPripremi.setMjesec(mjesecObracunaTemp.substring(4));
				obracunUPripremi.setStatus(0);
				obracunUPripremi.setMjesecObracuna(mjesecObracunaTemp);

				parm.clear();
				parm.add(mjesecObracunaTemp);
				rs = (ResultSet) trx.executePreparedQuerySnapShotById(moduleName,
						"prisutnostStatusObracunskiPeriodUMjesecuSelect", parm);
				obracunskiPeriod = 1;
				obracunUPripremi.setMustInsert(true);
				if (rs.next()) {
					obracunskiPeriod = rs.getInt("ObracunskiPeriod") + (rs.getInt("Status") > 0 ? 1 : 0);
					if (rs.getInt("Status") == 0)
						obracunUPripremi.setMustInsert(false);
				}
				obracunUPripremi.setObracunskiPeriod(obracunskiPeriod);
				lObracunUPripremi.add(obracunUPripremi);
			}
			userData.put(TRANS_LIST_OBRACUN_U_PRIPREMI, lObracunUPripremi);
		}
		parm.clear();
		parm.add(Arrays.asList(new Integer[] { 1, 2 }));
		rs = (ResultSet) trx.executePreparedQuerySnapShotById(moduleName, "prisutnostStatusSelect", parm);
		while (rs.next()) {
			obracunSvi = new StatusPrisutnost();
			tempMO = rs.getString("MjesecObracuna");
			obracunSvi.setMjesecObracuna(tempMO);
			obracunSvi.setGodina(tempMO.substring(0, 4));
			obracunSvi.setMjesec(tempMO.substring(4));
			obracunSvi.setObracunskiPeriod(rs.getInt("ObracunskiPeriod"));
			obracunSvi.setStatus(rs.getInt("Status"));
			lObracunSvi.add(obracunSvi);
		}
		userData.put(TRANS_LIST_OBRACUN_SVI, lObracunSvi);
		lOJ = sqlSnippetOJSelect(trx, rs, parm, lUserOJs, datumDohvata);



		log.debug("MAP CODE > -- sqlSnippetVRSelect - dohvat vrsti rada iz baze (reach the type of work from the database)--");
		lVrstaRada = sqlSnippetVRSelect(trx,rs,parm);

		parm.clear();
		parm.addAll(Arrays.asList(username));
		rs = (ResultSet) trx.executePreparedQuerySnapShotById(moduleName, "sifraPisRadnikaTest", parm);
		while (rs.next()) {
			LoginPoduzece = rs.getString(6);

		}
		//System.out.println("SIFRA KORISNIKA IZ PISA : "+SifraPis);
		if(LoginPoduzece.equals("PLINACRO d.o.o."))
		{
		userData.put(TRANS_ODABIR_VALUE, "Plinacro d.o.o.");
		}
		else if(LoginPoduzece.equals("Podzemno skladiĹˇte plina d.o.o."))
		{
		userData.put(TRANS_ODABIR_VALUE, "PSP d.o.o");
		}
		userData.put(TRANS_DATUM_DOHVATA, datumDohvata);
		userData.put(TRANS_LIST_NAZIV_OJ, lOJ);
		userData.put(TRANS_DATADUMPSUMARNO, null);
		userData.put(TRANS_DATA_INITIALIZED, true);
		userData.put(TRANS_LIST_VRSTERADA, lVrstaRada);

	} catch (Exception e) {
		throw new BusinessException(e.toString(), e);
	} finally {
		if (trx != null)
			trx.closeIfPossible();
	}
}

private List<VrstaRada> sqlSnippetVRSelect(ITransaction trx, ResultSet rs, List<Object> parm)
throws DatabaseException,ModuleException,SQLException
{
	List<VrstaRada> lVRRet = new ArrayList<VrstaRada>();
	parm.clear();
	VrstaRada vr = null;
	parm.addAll(java.util.Arrays.asList(new Object[] {}));
	//rs = (ResultSet)trx.executePreparedQuerySnapShotById(moduleName, "VrsteRadaAllSelect", parm);
	rs = (ResultSet)trx.executePreparedQuerySnapShotById(moduleName, "VrsteRadaAllSelectInt", parm);
	//System.out.println("RS VRSTE RADA : "+rs);
	while(rs.next())
	{
		//System.out.println("--"+rs.getString("naziv")+" --"+rs.getBoolean("zaKalendarRada")+" -- "+rs.getBoolean("zaPrikazDjelatnik")+" -- "+rs.getString("nazivApp")+" -- "+rs.getString("sifraVrsteRada"));
		vr = new VrstaRada();
		vr.setNaziv(rs.getString("naziv"));
		vr.setZaKalendarRada(rs.getBoolean("zaKalendarRada"));
		vr.setZaPrikazDjelatnika(rs.getBoolean("zaPrikazDjelatnik"));
		vr.setNazivApp(rs.getString("nazivApp"));
		vr.setSifraVrsteRada(rs.getString("sifraVrsteRada"));
		lVRRet.add(vr);
	}
	return lVRRet;
}

private void kludgeSnippet(Map<String, Object> userData, String mjesecObracuna, boolean history) {
	Map<Integer, Boolean> mMjesecPraznici = new LinkedHashMap<Integer, Boolean>();
	List<Integer> lIKludge = new ArrayList<Integer>();
	List<String> lWorkdayKludge = new ArrayList<String>();
	userData.put(history ? TRANS_MJESEC_PRAZNICI_HISTORY : TRANS_MJESEC_PRAZNICI, mMjesecPraznici);
	List<List> lDaysInMonthInfoKludge = getDaysInMonthInfoKludge(userData, mMjesecPraznici, mjesecObracuna);
	lIKludge = lDaysInMonthInfoKludge.get(0);
	lWorkdayKludge = lDaysInMonthInfoKludge.get(1);
	userData.put(history ? TRANS_LIST_I_KLUDGE_HISTORY : TRANS_LIST_I_KLUDGE, lIKludge);
	userData.put(history ? TRANS_LIST_WORKDAY_KLUDGE_HISTORY : TRANS_LIST_WORKDAY_KLUDGE, lWorkdayKludge);
}

private List<OrgJedinica> sqlSnippetOJSelect(ITransaction trx, ResultSet rs, List<Object> parm, List<String> lUserOJs,
		java.sql.Date datumDohvata) throws DatabaseException, ModuleException, SQLException {
	System.out.println("----- "+trx+" ----- "+rs+" ----- "+parm+" ----- "+lUserOJs+" ------ "+datumDohvata);
	List<OrgJedinica> lRet = new ArrayList<OrgJedinica>();
	parm.clear();
	OrgJedinica oj = null;
	if (!lUserOJs.contains("SVE")) {
		parm.addAll(java.util.Arrays.asList(new Object[] { datumDohvata, datumDohvata, lUserOJs, datumDohvata }));
	} else {
		parm.addAll(java.util.Arrays.asList(new Object[] { datumDohvata, datumDohvata, datumDohvata }));
	}
	rs = (ResultSet) trx.executePreparedQuerySnapShotById(moduleName, lUserOJs.contains("SVE") ? "ojAllSelect"
			: "ojSelect", parm); //ojAllSelect ojAllNaDanSelect
	while (rs.next()) {
		oj = new OrgJedinica();
		oj.setNaziv(rs.getString("NazivOJ"));
		oj.setSifraOJ(rs.getString("SifraOJ"));
		if(rs.getString("NAZIV_PODUZECA").equals("Podzemno skladiĹˇte plina d.o.o."))
		{
			//System.out.println(" -------- "+rs.getString("NAZIV_PODUZECA"));
			oj.setNazivPod("PSP d.o.o");
		}
		else
		{
		oj.setNazivPod(rs.getString("NAZIV_PODUZECA"));
		//System.out.println("ELSE -------- "+rs.getString("NAZIV_PODUZECA"));
		}
		lRet.add(oj);
	}

	return lRet;
}

private List<Zaposlenik> sqlSnippetRadniciUOJSelect(ITransaction trx, ResultSet rs, List<Object> parm,
		String mjesecObracuna, Integer obracunskiPeriod, String sifraOJ, java.sql.Date datumDohvata)
		throws SQLException, BaseFrameworkException {
	List<Zaposlenik> lRet = new ArrayList<Zaposlenik>();
	Zaposlenik zaposlenik = null;
	parm.clear();
	parm.addAll(Arrays.asList(datumDohvata,datumDohvata, datumDohvata, datumDohvata, datumDohvata, datumDohvata, datumDohvata,
			datumDohvata, mjesecObracuna, obracunskiPeriod, sifraOJ, datumDohvata, datumDohvata, datumDohvata,
			datumDohvata, datumDohvata, datumDohvata, datumDohvata, datumDohvata, datumDohvata, datumDohvata,
			datumDohvata, datumDohvata));
	System.out.println("RADNICI U ORG: JEDINICI");
	rs = (ResultSet) trx.executePreparedQuerySnapShotById(moduleName, "radniciUOJSelect", parm);
	while (rs.next()) {
		zaposlenik = fillZaposlenikSnippet(zaposlenik, rs, lRet);
	}
	System.out.println("rezultat upita : "+lRet);
	return lRet;
}

private List<Zaposlenik> sqlSnippetRadniciUOJDnevnoSelect(ITransaction trx, ResultSet rs, List<Object> parm,
		 String sifraOJ, java.sql.Date datumDohvata)
		throws SQLException, BaseFrameworkException {
	//System.out.println("uslo u sqlSnippetRadniciUOJSelect : "+trx);
	List<Zaposlenik> lRet = new ArrayList<Zaposlenik>();
	Zaposlenik zaposlenik = null;
	parm.clear();
	parm.addAll(Arrays.asList(datumDohvata, datumDohvata, datumDohvata, datumDohvata, datumDohvata, datumDohvata,
			datumDohvata, sifraOJ, datumDohvata, datumDohvata, datumDohvata,
			datumDohvata, datumDohvata, datumDohvata, datumDohvata, datumDohvata, datumDohvata, datumDohvata,
			datumDohvata, datumDohvata));
	rs = (ResultSet) trx.executePreparedQuerySnapShotById(moduleName, "radniciUOJDnevnoSelect", parm);
	//System.out.println("sql Snippet Radnici U OJ Select rs : "+rs);
	while (rs.next()) {
		System.out.println("IME : "+rs.getString("PrezIme"));
		zaposlenik = fillZaposlenikSnippet(zaposlenik, rs, lRet);
	}
	return lRet;
}

private List<OrgJedinicaRadniKalendar> sqlSnippetRadniKalendarRadnikaUOJSelect(ITransaction trx, ResultSet rs, List<Object> parm, String sifraOJ,Date datumDohvataDnevno, List<Zaposlenik> zaposlenik,String danIzmjeneDnevno, Map<String, Object> userData) throws SQLException
{

	String orgJedinicaSifra = (String)userData.get(TRANS_SIFRAOJ);
	List<OrgJedinicaRadniKalendar> lRadnogKalendaraOJ = new ArrayList<OrgJedinicaRadniKalendar>();
	OrgJedinicaRadniKalendar listRadKalOJ = null;
	parm.clear();
	boolean isWeekend = false;
	boolean isHoliday = false;
	int j = 1;
	try {
		parm.addAll(java.util.Arrays.asList(new Object[] {datumDohvataDnevno, datumDohvataDnevno,datumDohvataDnevno,datumDohvataDnevno,datumDohvataDnevno,datumDohvataDnevno,datumDohvataDnevno,orgJedinicaSifra,datumDohvataDnevno,datumDohvataDnevno,datumDohvataDnevno,datumDohvataDnevno,datumDohvataDnevno,datumDohvataDnevno,datumDohvataDnevno,datumDohvataDnevno,datumDohvataDnevno,datumDohvataDnevno,datumDohvataDnevno,datumDohvataDnevno,datumDohvataDnevno}));
		//rs = (ResultSet) trx.executePreparedQuerySnapShotById(moduleName, "izvadakEvidRadSatSelect", parm);
		rs = (ResultSet) trx.executePreparedQuerySnapShotById(moduleName, "radniKalendarDnevnoSelect", parm);
		System.out.println(" ----- sifraOJ : "+sifraOJ);
		System.out.println("------ datumDohvata : "+datumDohvataDnevno);

		Calendar isDay = Calendar.getInstance();
		isDay.setTime(datumDohvataDnevno);
		isDay.add(Calendar.DATE, 0);
		isWeekend = UtilDate.isWeekend(isDay);
		isHoliday = UtilDate.isHoliday(isDay);
		System.out.println(" ---------------- ffffffffffffffff ---------------------");

		for(int i = 0; i<= zaposlenik.size();i++)
		{
			listRadKalOJ = new OrgJedinicaRadniKalendar();
			if(rs.absolute(j)&& rs.getString("PrezIme").equals(zaposlenik.get(i).getPrezimeImeZap()))
			{

				listRadKalOJ.setIDzaposlenikUnos(rs.getString("IDzaposlenikUnos"));
				listRadKalOJ.setPrezimeIme(rs.getString("PrezIme"));
				listRadKalOJ.setVr01ORK(rs.getString("vr01"));
				listRadKalOJ.setVr02ORK(rs.getString("vr02"));
				listRadKalOJ.setVr03ORK(rs.getBigDecimal("vr03"));
				listRadKalOJ.setVr04ORK(rs.getString("vr04"));
				listRadKalOJ.setVr05ORK(rs.getString("vr05"));
				listRadKalOJ.setVr06ORK(rs.getBigDecimal("vr06"));
				listRadKalOJ.setVr07ORK(rs.getBigDecimal("vr07"));
				listRadKalOJ.setVr08ORK(rs.getBigDecimal("vr08"));
				listRadKalOJ.setVr09ORK(rs.getBigDecimal("vr09"));
				listRadKalOJ.setVr10ORK(rs.getBigDecimal("vr10"));
				listRadKalOJ.setVr11ORK(rs.getBigDecimal("vr11"));
				listRadKalOJ.setVr12ORK(rs.getBigDecimal("vr12"));
				listRadKalOJ.setVr13ORK(rs.getBigDecimal("vr13"));
				listRadKalOJ.setVr14ORK(rs.getBigDecimal("vr14"));
				listRadKalOJ.setVr15ORK(rs.getBigDecimal("vr15"));
				listRadKalOJ.setVr16ORK(rs.getBigDecimal("vr16"));
				listRadKalOJ.setVr17ORK(rs.getBigDecimal("vr17"));
				listRadKalOJ.setVr18ORK(rs.getBigDecimal("vr18"));
				listRadKalOJ.setVr19ORK(rs.getBigDecimal("vr19"));
				listRadKalOJ.setVr20ORK(rs.getString("vr20"));
				listRadKalOJ.setVr21ORK(rs.getBigDecimal("vr21"));
				listRadKalOJ.setVr48ORK(rs.getBigDecimal("vr48"));
				listRadKalOJ.setVr22ORK(rs.getBigDecimal("vr22"));
				listRadKalOJ.setVr23ORK(rs.getBigDecimal("vr23"));
				listRadKalOJ.setVr24ORK(rs.getBigDecimal("vr24"));
				listRadKalOJ.setVr25ORK(rs.getBigDecimal("vr25"));
				listRadKalOJ.setVr26ORK(rs.getBigDecimal("vr26"));
				listRadKalOJ.setVr27ORK(rs.getBigDecimal("vr27"));
				listRadKalOJ.setVr28ORK(rs.getBigDecimal("vr28"));
				listRadKalOJ.setVr29ORK(rs.getBigDecimal("vr29"));
				listRadKalOJ.setVr30ORK(rs.getBigDecimal("vr30"));
				listRadKalOJ.setVr31ORK(rs.getBigDecimal("vr31"));
				listRadKalOJ.setVr32ORK(rs.getBigDecimal("vr32"));
				listRadKalOJ.setVr33ORK(rs.getBigDecimal("vr32"));
				listRadKalOJ.setVr34ORK(rs.getBigDecimal("vr34"));
				listRadKalOJ.setVr35ORK(rs.getBigDecimal("vr35"));
				listRadKalOJ.setVr36ORK(rs.getBigDecimal("vr36"));
				listRadKalOJ.setVr37ORK(rs.getBigDecimal("vr37"));
				listRadKalOJ.setVr38ORK(rs.getBigDecimal("vr38"));
				listRadKalOJ.setVr39ORK(rs.getBigDecimal("vr39"));
				listRadKalOJ.setVr40ORK(rs.getBigDecimal("vr40"));
				listRadKalOJ.setVr41ORK(rs.getBigDecimal("vr41"));
				listRadKalOJ.setVr42ORK(rs.getBigDecimal("vr42"));
				listRadKalOJ.setVr43ORK(rs.getBigDecimal("vr43"));
				listRadKalOJ.setVr44ORK(rs.getBigDecimal("vr44"));
				listRadKalOJ.setVr45ORK(rs.getBigDecimal("vr45"));
				listRadKalOJ.setVr46ORK(rs.getBigDecimal("vr46"));
				listRadKalOJ.setVr47ORK(rs.getString("vr47"));

				if (isWeekend)
				{
					listRadKalOJ.setVikendNeradniDanORK(true);
					//System.out.println(" praznik :"+rk.isVikendNeradniDan());
				}
				else if(isHoliday)
				{
						listRadKalOJ.setVikendNeradniDanORK(true);
				}
				listRadKalOJ.setUnesenoUBazuORK(true);
				System.out.println(i+" zaposlenik iz baze: "+rs.getString("IDZaposlenikUnos")+" - "+rs.getString("PrezIme"));
				j++;
			}
			else
			{


				listRadKalOJ.setIDzaposlenikUnos(zaposlenik.get(i).getBrojZaposlenika());
				listRadKalOJ.setPrezimeIme(zaposlenik.get(i).getPrezimeImeZap());

				if(sifraOJ.subSequence(0, 1).equals("V"))
					{
					listRadKalOJ.setVr01ORK("07:00");
					listRadKalOJ.setVr02ORK("15:00");
					listRadKalOJ.setVr04ORK("07:00");
					listRadKalOJ.setVr05ORK("15:00");
					if(zaposlenik.get(i).getTerenskiRadnik()&& isWeekend==false)
						{
							if(isHoliday==false)
							{
								listRadKalOJ.setVr20ORK("T37");
							}
						}
					}
				else if (sifraOJ.subSequence(0, 1).equals("0"))
					{
					if(sifraOJ.subSequence(0, 5).equals("00002"))
						{
							listRadKalOJ.setVr01ORK("07:00");
							listRadKalOJ.setVr02ORK("15:00");
							listRadKalOJ.setVr04ORK("07:00");
							listRadKalOJ.setVr05ORK("15:00");
							//System.out.println("----------------- 2");
							if(zaposlenik.get(i).getTerenskiRadnik()&& isWeekend==false)
							{
								if(isHoliday==false)
								{
									listRadKalOJ.setVr20ORK("T37");
								}
							}
						}
					else
						{
						listRadKalOJ.setVr01ORK("07:30");
						listRadKalOJ.setVr02ORK("15:30");
						listRadKalOJ.setVr04ORK("07:30");
						listRadKalOJ.setVr05ORK("15:30");
						if(zaposlenik.get(i).getTerenskiRadnik()&& isWeekend==false)
						{
							if(isHoliday==false)
							{
								listRadKalOJ.setVr20ORK("T37");
							}
						}
						}
					}
				else
					{
					listRadKalOJ.setVr01ORK("");
					listRadKalOJ.setVr02ORK("");
					listRadKalOJ.setVr04ORK("00:00");
					listRadKalOJ.setVr05ORK("00:00");
					if(zaposlenik.get(i).getTerenskiRadnik()&& isWeekend==false)
					{
						if(isHoliday==false)
						{
							listRadKalOJ.setVr20ORK("T37");
						}
					}
					}
				if (isWeekend)
					{
					listRadKalOJ.setVr01ORK("");
					listRadKalOJ.setVr02ORK("");
					listRadKalOJ.setVikendNeradniDanORK(true);
					listRadKalOJ.setVr03ORK(new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP));
					}
				else if(isHoliday)
					{
					listRadKalOJ.setVr01ORK("");
					listRadKalOJ.setVr02ORK("");
					listRadKalOJ.setVikendNeradniDanORK(true);
					listRadKalOJ.setVr03ORK(new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP));
					listRadKalOJ.setVr18ORK(new BigDecimal(8.00).setScale(2, BigDecimal.ROUND_UP));
					}
				else
					{
						if(sifraOJ.subSequence(0, 1).equals("V"))
							{
								if(sifraOJ.equals("V100"))
									{
									listRadKalOJ.setVr01ORK("07:30");
									listRadKalOJ.setVr02ORK("15:30");
									listRadKalOJ.setVr04ORK("07:30");
									listRadKalOJ.setVr05ORK("15:30");
									if(zaposlenik.get(i).getTerenskiRadnik()&& isWeekend==false)
									{
										if(isHoliday==false)
										{
											listRadKalOJ.setVr20ORK("T37");
										}
									}
									}
								else
									{
									listRadKalOJ.setVr01ORK("07:00");
									listRadKalOJ.setVr02ORK("15:00");
									listRadKalOJ.setVr04ORK("07:00");
									listRadKalOJ.setVr05ORK("15:00");
									if(zaposlenik.get(i).getTerenskiRadnik()&& isWeekend==false)
									{
										if(isHoliday==false)
										{
											listRadKalOJ.setVr20ORK("T37");
										}
									}
									}
							}
						else if(sifraOJ.subSequence(0, 1).equals("0"))
						{
							if(sifraOJ.subSequence(0, 5).equals("00002"))
								{
								listRadKalOJ.setVr01ORK("07:00");
								listRadKalOJ.setVr02ORK("15:00");
								listRadKalOJ.setVr04ORK("07:00");
								listRadKalOJ.setVr05ORK("15:00");
								if(zaposlenik.get(i).getTerenskiRadnik()&& isWeekend==false)
								{
									if(isHoliday==false)
									{
										listRadKalOJ.setVr20ORK("T37");
									}
								}
								}
							else
								{
								listRadKalOJ.setVr01ORK("07:30");
								listRadKalOJ.setVr02ORK("15:30");
								listRadKalOJ.setVr04ORK("07:30");
								listRadKalOJ.setVr05ORK("15:30");
								if(zaposlenik.get(i).getTerenskiRadnik()&& isWeekend==false)
								{
									if(isHoliday==false)
									{
										listRadKalOJ.setVr20ORK("T37");
									}
								}
								}
						}
						else
							{
								listRadKalOJ.setVr01ORK("07:30");
								listRadKalOJ.setVr02ORK("15:30");
								listRadKalOJ.setVr04ORK("07:30");
								listRadKalOJ.setVr05ORK("15:30");
								if(zaposlenik.get(i).getTerenskiRadnik()&& isWeekend==false)
								{
									if(isHoliday==false)
									{
										listRadKalOJ.setVr20ORK("T37");
									}
								}
							}

						if (isWeekend)
						{
						listRadKalOJ.setVr01ORK("");
						listRadKalOJ.setVr02ORK("");
						listRadKalOJ.setVikendNeradniDanORK(true);
						listRadKalOJ.setVr03ORK(new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP));
						}
					else if(isHoliday)
						{
						listRadKalOJ.setVr01ORK("");
						listRadKalOJ.setVr02ORK("");
						listRadKalOJ.setVikendNeradniDanORK(true);
						listRadKalOJ.setVr03ORK(new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP));
						listRadKalOJ.setVr18ORK(new BigDecimal(8.00).setScale(2, BigDecimal.ROUND_UP));
						}

						listRadKalOJ.setVr03ORK(new BigDecimal(8.00).setScale(2, BigDecimal.ROUND_UP));
						listRadKalOJ.setVr06ORK(new BigDecimal(8.00).setScale(2, BigDecimal.ROUND_UP));
					}

				listRadKalOJ.setUnesenoUBazuORK(false);
				System.out.println(i+"zaposlenik nije u bazi : "+zaposlenik.get(i).getPrezimeImeZap()+" terenac : "+zaposlenik.get(i).getTerenskiRadnik());
			}

			lRadnogKalendaraOJ.add(listRadKalOJ);
			isDay.clear();

		}

		System.out.println("------ danIzmjeneDnevno : "+listRadKalOJ);

	} catch (Exception e) {
		System.out.println(e.getMessage());
	}
	finally{
		if (trx != null)
			trx.closeIfPossible();
	}
	return lRadnogKalendaraOJ;
}

	private static EvidencijaRadaPortletSessionBean getSessionBean(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		if( session == null )
			return null;
		EvidencijaRadaPortletSessionBean sessionBean = (EvidencijaRadaPortletSessionBean)session.getAttribute(SESSION_BEAN);
		if( sessionBean == null ) {
			sessionBean = new EvidencijaRadaPortletSessionBean();
			session.setAttribute(SESSION_BEAN,sessionBean);
		}
		return sessionBean;
	}

	private static String getJspFilePath(RenderRequest request, String jspFile) {
		String markup = request.getProperty("wps.markup");
		if( markup == null )
			markup = getMarkup(request.getResponseContentType());
		return JSP_FOLDER + markup + "/" + jspFile + "." + getJspExtension(markup);
	}

	private static String getMarkup(String contentType) {
		if( "text/vnd.wap.wml".equals(contentType) )
			return "wml";
        else
            return "html";
	}

	private static String getJspExtension(String markupName) {
		return "jsp";
	}

	private List<List> getDaysInMonthInfoKludge(Map<String, Object> userData, Map<Integer, Boolean> mMjesecPraznici,
			String mjesecObracuna) {
		List<List> lRet = new ArrayList<List>();
		List<Integer> lIKludge = new ArrayList<Integer>();
		List<String> lWorkdayKludge = new ArrayList<String>();
		boolean isWeekend = false;
		boolean isHoliday = false;
		boolean isSaturday = false;

		int year = UtilEvidencijaRada.getMjesecObracunaGodina(mjesecObracuna);
		int month = UtilEvidencijaRada.getMjesecObracunaMjesec(mjesecObracuna);
		int daysInMonth = UtilDate.lastDayOfMonthInt(year, month);

		for (int dayInField = 1; dayInField <= daysInMonth; dayInField++) {
			Calendar cal = Calendar.getInstance();
			cal.set(year, month - 1, dayInField);
			isWeekend = UtilDate.isWeekend(cal);
			System.out.println("-----------------------------   "+cal);
			try {
				isHoliday = UtilDate.isHoliday(cal);
			} catch (KCacheException e) {
				UtilConsole.printStackTrace(e);
			}
			//if (initialPass)
			mMjesecPraznici.put(dayInField, isHoliday);
			isSaturday = UtilDate.isWeekend(cal, true);

			if (isHoliday && !isWeekend) {
				lIKludge.add(dayInField + 200);
				lWorkdayKludge.add("2");

			} else if (isSaturday) {
				lIKludge.add(dayInField + 800);
				lWorkdayKludge.add("3");
			} else if (isWeekend) {
				lIKludge.add(dayInField + 100);
				lWorkdayKludge.add("1");
			} else {
				lIKludge.add(dayInField);
				lWorkdayKludge.add("0");
			}
		}
		lRet.add(lIKludge);
		lRet.add(lWorkdayKludge);
		return lRet;
	}

	private Zaposlenik fillZaposlenikSnippet(Zaposlenik zaposlenik, ResultSet rs, List<Zaposlenik> lZaposlenik)
	throws SQLException, BaseFrameworkException {
		System.out.println("fillZAposlenikSnipet rs : "+rs);
		Zaposlenik beanDummyZaposlenik = new Zaposlenik();
		zaposlenik = beanDataHandler.getBeanFromResultSet(beanDummyZaposlenik, rs);
		lZaposlenik.add(zaposlenik);
		return zaposlenik;
	}


	private void fillPrisutnostZaposlenikPovijestSnippet(ResultSet rs,
			List<PrisutnostZaposlenikPovijest> lPrisutnostZaposlenikPovijest) throws SQLException, BaseFrameworkException {
		PrisutnostZaposlenikPovijest beanDummyPrisutnostZaposlenikPovijest = new PrisutnostZaposlenikPovijest();
		lPrisutnostZaposlenikPovijest.add(beanDataHandler.getBeanFromResultSet(beanDummyPrisutnostZaposlenikPovijest, rs));
	}
	private void updateZaposlenikDanUnosaPrviZadnji(Zaposlenik zaposlenik, Map<String, Object> userData,
			String mjesecObracuna) {
		int danUnosaPrvi = 0;
		int danUnosaZadnji = 0;
		boolean promjenaRO = false;
		Calendar calMjesecObracuna = new GregorianCalendar();
		//calMjesecObracuna = new GregorianCalendar((Integer) userData.get(TRANS_MJESEC_OBRACUNA_GODINA), ((Integer) userData
		//		.get(TRANS_MJESEC_OBRACUNA_MJESEC)) - 1, 1);
		calMjesecObracuna = new GregorianCalendar(UtilEvidencijaRada.getMjesecObracunaGodina(mjesecObracuna), (UtilEvidencijaRada
				.getMjesecObracunaMjesec(mjesecObracuna)) - 1, 1);
		calMjesecObracuna.set(Calendar.YEAR, calMjesecObracuna.get(Calendar.YEAR));
		calMjesecObracuna.set(Calendar.MONTH, calMjesecObracuna.get(Calendar.MONTH));
		calMjesecObracuna.set(Calendar.DAY_OF_MONTH, calMjesecObracuna.get(Calendar.DAY_OF_MONTH));
		System.out.println(" HHHHHHHHHH ::"+calMjesecObracuna);
		Calendar calROTemp = new GregorianCalendar();
		calROTemp.setTime(zaposlenik.getPocetakRO()); //TODO: provjeriti je li ovo ispravno, tj je li ovo datum poÄŤetka rada u firmi ili datum promjene rasporeda
		promjenaRO = (calROTemp.get(Calendar.YEAR) == calMjesecObracuna.get(Calendar.YEAR) && calROTemp.get(Calendar.MONTH) == calMjesecObracuna
				.get(Calendar.MONTH));
		danUnosaPrvi = promjenaRO ? calROTemp.get(Calendar.DAY_OF_MONTH) : 1;
		promjenaRO = false;
		if (zaposlenik.getZavrstetakRO() != null) {
			calROTemp.setTime(zaposlenik.getZavrstetakRO());
			promjenaRO = (calROTemp.get(Calendar.YEAR) == calMjesecObracuna.get(Calendar.YEAR) && calROTemp
					.get(Calendar.MONTH) == calMjesecObracuna.get(Calendar.MONTH));
		}
		danUnosaZadnji = promjenaRO ? calROTemp.get(Calendar.DAY_OF_MONTH) : calMjesecObracuna
				.getActualMaximum(Calendar.DAY_OF_MONTH);
		userData.put(TRANS_ZAPOSLENIK_DAN_UNOSA_PRVI, danUnosaPrvi);
		userData.put(TRANS_ZAPOSLENIK_DAN_UNOSA_ZADNJI, danUnosaZadnji);
	}
	private void clearTransientData(Map<String, Object> userData) {
		userData.remove(TRANS_SIFRAOJ);
		userData.remove(TRANS_LIST_ZAPOSLENIK);
		userData.remove(TRANS_IMEPREZIME);
		userData.remove(TRANS_BROJZAPOSLENIKA);
		userData.remove(TRANS_STATUSPRISUTNOSTIZAPOSLENIKA);
		userData.remove(TRANS_TERENSKIRADNIK);
		userData.remove(TRANS_BEAN_ZAPOSLENIK);
		userData.remove(TRANS_PODACIUBAZI);
		userData.remove(TRANS_DATADUMPANALITIKA);
		userData.remove(TRANS_DATADUMPSUMARNO);
		userData.remove(TRANS_PODACIUBAZI);
		userData.remove(TRANS_LIST_DATADUMPSUMARNO);
		userData.remove(TRANS_LIST_DATADUMPANALITIKA);
		userData.remove(TRANS_LIST_PRISUTNOST_ZAPOSLENIK_POVIJEST);
		userData.remove(TRANS_ZAPOSLENIK_DAN_UNOSA_PRVI);
		userData.remove(TRANS_ZAPOSLENIK_DAN_UNOSA_ZADNJI);
		userData.remove(TRANS_MJESEC_OBRACUNA_HISTORY);
		userData.remove(TRANS_OBRACUNSKI_PERIOD_HISTORY);
		userData.remove(TRANS_DATUM_DOHVATA_HISTORY);
		userData.remove(TRANS_LIST_NAZIV_OJ_HISTORY);
		userData.remove(TRANS_LIST_STATUS_PRISUTNOSTI_PO_SEKTORIMA);
		userData.remove(TRANS_STATUS_PRISUTNOSTI_SKUPNO);
	}
	private void clearTransientBadEnter(Map<String, Object> userData) {
		//userData.remove(TRANS_SIFRAOJ);
		//userData.remove(TRANS_LIST_ZAPOSLENIK);
		//userData.remove(TRANS_IMEPREZIME);
		//userData.remove(TRANS_BROJZAPOSLENIKA);
		//userData.remove(TRANS_TERENSKIRADNIK);
		userData.remove(TRANS_BEAN_ZAPOSLENIK);
		userData.remove(TRANS_STRING_UKUPNE_VRIJEDNOSTI);
		userData.remove(TRANS_LIST_ZAPOSLENIK_RADNI_KALENDAR);
		//userData.remove(TRANS_LIST_VRSTERADA);
		//userData.remove(TRANS_TABLICA_EVID_VIEW);
	}

	private boolean compareData(java.sql.Date periodOd, java.sql.Date periodDo, Map<String, Object> userData)
	{

		Calendar data1 = Calendar.getInstance();
		Calendar data2 = Calendar.getInstance();
		String Data_format_now = "yyyy-MM-dd";
		Calendar calNow = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(Data_format_now);
		String datumTrenutni = sdf.format(calNow.getTime());
		java.sql.Date sqlTrenDatum = java.sql.Date.valueOf(datumTrenutni);
		Calendar datTren = Calendar.getInstance();


		data1.setTime(periodOd);
		data2.setTime(periodDo);
		datTren.setTime(sqlTrenDatum);

		if(datTren.after(data1)|| datTren.equals(data1))
			{
			//System.out.println("IF MANJE ILI JEDNAKO TRENUTAÄŚNOM DATUMU - TRUE");
				if(datTren.after(data2) || datTren.equals(data2))
				{
					//System.out.println("IF DATUM 2 MANJI ILI JEDNAK TRENUTNOM DATUMU - TRUE");
					if(data1.before(data2) )
						{
						//System.out.println("DATUM 1 MANJI OD DATUMA 2 - TRUE");
						return true;
						}
					else if(data1.equals(data2))
						{
						//System.out.println("DATUM 1 JEDNAK DATUMU 2 - TRUE");
						return true;
						}
					else
					{
					//System.out.println("DATUM 1 NIJE NITI MANJI NITI JEDNAK - FALSE");
					userData.put(TRANS_LOS_UNOS_MSG, "Krivo uneseni vremenski period - datum u polju Od : je veÄ‡i od datuma u polju Do : ili forma nema unos.");
					return false;
					}
				}
				else{
					//System.out.println("IF DATUM 2 MANJI ILI JEDNAK TRENUTNOM DATUMU - FALSE");
					userData.put(TRANS_LOS_UNOS_MSG, "Datum u formi unosa vremenskog perioda je veÄ‡i od trenutaÄŤnog datuma.");
					return false;}
				}
		else{
			//System.out.println("VEÄ†I OD TRENUTAÄŚNOG DATUMA - FALSE");
			userData.put(TRANS_LOS_UNOS_MSG, "Datum u formi unosa vremenskog perioda je veći od trenutačnog datuma.");
				return false;
			}
	}

	private List<RadniKalendarNew> sqlSnippetIzvadakEvidRadSatSelect (ITransaction trx, ResultSet rs, List<Object> parm, java.sql.Date periodOd, java.sql.Date periodDo, String IDzaposlenikUnos,Map<String, Object> userData)
	throws SQLException, BaseFrameworkException
		{
		//System.out.println("nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
		String sifraOrgJed = (String)userData.get(TRANS_SIFRAOJ);
		//List<RadniKalendar> lZRK = new ArrayList<RadniKalendar>();
		List<RadniKalendarNew> lZRK = new ArrayList<RadniKalendarNew>();
		List<UkupnoSati> lUkupnoSati = new ArrayList<UkupnoSati>();
		parm.clear();
		//RadniKalendar rk = null;
		RadniKalendarNew rk = null;
		UkupnoSati us = null;

		/* Stari podaci za app radni kalendar
		double uvr04 = 0.00; double uvr05 = 0.00;double uvr06 = 0.00;double uvr07=0.00; double uvr09 = 0.00;
		double uvr10 = 0.00; double uvr14 = 0.00; double uvr15=0.00; double uvr20 = 0.00; double uvr24 = 0.00;
		double uvr25 = 0.00; double uvr26 = 0.00; double uvr32 = 0.00; double uvr40 = 0.00; double uvr41 = 0.00;
		double uvr42 = 0.00; double uvr43 = 0.00; double uvr44 = 0.00; double uvr45 = 0.00; double uvr46 = new BigDecimal(0.00);
		*/

		//novi podaci
		BigDecimal uvr03 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP); BigDecimal uvr06 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP);BigDecimal uvr07=new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP);
		BigDecimal uvr08 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP); BigDecimal uvr09 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP);BigDecimal uvr10 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP);
		BigDecimal uvr11 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP); BigDecimal uvr12=new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP);  BigDecimal uvr13 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP);
		BigDecimal uvr14 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP); BigDecimal uvr15 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP); BigDecimal uvr16 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP);
		BigDecimal uvr17 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP); BigDecimal uvr18 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP); BigDecimal uvr19 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP);
		BigDecimal uvr20 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP); BigDecimal uvr21 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP); BigDecimal uvr48 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP); BigDecimal uvr22 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP);
		BigDecimal uvr23 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP); BigDecimal uvr24 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP); BigDecimal uvr25 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP);
		BigDecimal uvr26 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP); BigDecimal uvr27 = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP);
		DecimalFormat df = new DecimalFormat("#.##");
		int brojOdabranihDana = calculateDifference(periodOd,periodDo);

		System.out.println(periodOd+" Broj odabranih dana : "+brojOdabranihDana);
		try {
//			parm.addAll(java.util.Arrays.asList(new Object[] { periodOd, periodDo,IDzaposlenikUnos})); ovo odkomentirati

			parm.addAll(java.util.Arrays.asList(new Object[] { periodOd, periodDo,IDzaposlenikUnos}));

		  rs = (ResultSet) trx.executePreparedQueryById(moduleName, "izvadakEvidRadSatSelectInt", parm);//ovo je bilo zakomentirano

			//dio sa prepared statement




		//	rs = (ResultSet) trx.executePreparedQuerySnapShotById(moduleName, "izvadakEvidRadSatSelectInt", parm);

		System.out.println("RSSSSSSSSS ="+rs.getFetchSize());
			int j = 1;
			boolean isWeekend = false;
			boolean isHoliday = false;

			for(int i=1;i<=brojOdabranihDana+1;i++)
				{
					System.out.println(i+" ---- "+periodOd+" - "+periodDo);
					//rk = new RadniKalendar();
					rk = new RadniKalendarNew();
					Calendar isDay = Calendar.getInstance();
					System.out.println("RS Line number="+rs.getRow());
					isDay.setTime(periodOd);
					Integer broj = i;
					isDay.add(Calendar.DATE, broj-1);
					isWeekend = UtilDate.isWeekend(isDay);
					isHoliday = UtilDate.isHoliday(isDay);
					Integer mjesec = isDay.get(Calendar.MONTH)+1;
					//System.out.println("MJESEC : "+mjesec);
					String mjesString = null;//mjesec.toString();
					Integer dan = isDay.get(Calendar.DATE);
					String danString = null;
					System.out.println(" --------------11111111111111111111111111---------------- ");
					if(dan<=9)
						{
							danString = "0"+dan.toString();
						}
					else{
							danString = dan.toString();

						}
					if(mjesec<=9)
						{
							mjesString = "0"+mjesec.toString();
						}
					else
						{
							mjesString = mjesec.toString();
						}
					System.out.println("MJESEC DANA U TJEDNU : "+mjesString);
					Integer godina = isDay.get(Calendar.YEAR);
					String godinaString = godina.toString();
					String danUTjednu = danString+"."+mjesString+"."+godinaString;
					System.out.println("DAN U TJEDNU DATUM : "+danUTjednu);
					String nazivDanaHR = danUTjednu(danUTjednu);

					System.out.println(" =========         "+i+" - "+danUTjednu+"         ================"+isWeekend+" -- Praznik :"+isHoliday);
					//System.out.println("------ : "+rs.getString("DatumUnosa"));
					//System.out.println("((((((( "+rs.absolute(j)+" IIII "+rs.getString("DatumUnosa").equals(danUTjednu)+" -- "+rs.getString("DatumUnosa")+"--"+danUTjednu);
					try {
						System.out.println("(((((((IIII "+rs.getString("DatumUnosa")+" = "+danUTjednu);
					} catch (SQLException se) {
						System.out.println(se.getMessage());
					}


					if(rs.absolute(j)&&rs.getString("DatumUnosa").equals(danUTjednu))
						{
							System.out.println(rs.getString("DatumUnosa"));
							//if(rs.getString("DatumUnosa")==danUTjednu){
							//System.out.println("----Unutar RS-a : i= "+i+", rs.apsolute :"+rs.absolute(j)+", dan u Tjednu :"+danUTjednu+" = datum iz baze"+rs.getString("DatumUnosa")+" ----");
							//rk = new RadniKalendar();
						if(userData.get("zaposlenik_dan_unosa_prvi")==null)
						{
							userData.put("zaposlenik_dan_unosa_prvi", 1);
						}
						if(userData.get("zaposlenik_dan_unosa_zadnji")==null){
								userData.put("zaposlenik_dan_unosa_zadnji", isDay.getActualMaximum(Calendar.DAY_OF_MONTH));
						}


							rk.setIDzaposlenikUnos(rs.getString("IDzaposlenikUnos"));
							rk.setDatumUnosa(rs.getString("DatumUnosa"));
							System.out.println(" ---- "+rs.getString("DatumUnosa"));
							rk.setDanUTjednu(danUTjednu(rs.getString("DatumUnosa")));
							try {
								System.out.println("-----------222222222222222-------------"+userData.get("zaposlenik_dan_unosa_prvi"));
							} catch (Exception e) {
								System.out.println("-----333333333333333333----- "+e.getMessage());
							}

							//Integracija aplikacija prisutnost i radni kalendar (promjena novih poslovnih pravila u aplikaciji radni kalendar)
							if(Integer.parseInt(danString)<Integer.parseInt(userData.get("zaposlenik_dan_unosa_prvi").toString()) || Integer.parseInt(danString)>Integer.parseInt(userData.get("zaposlenik_dan_unosa_zadnji").toString()))
							{
							rk.setVr01("");
							rk.setVr02("");
							rk.setVr03(new BigDecimal("0.00"));
							uvr03 = uvr03.add(new BigDecimal("0.00"));;
							rk.setVr04("");
							rk.setVr05("");
							rk.setVr06(new BigDecimal("0.00"));
							uvr06 = uvr06.add(new BigDecimal("0.00"));
							}
							else{
								rk.setVr01(rs.getString("vr01"));
								rk.setVr02(rs.getString("vr02"));
								rk.setVr03(rs.getBigDecimal("vr03"));
								uvr03 = uvr03.add(rs.getBigDecimal("vr03"));new BigDecimal("0.00");
								rk.setVr04(rs.getString("vr04"));
								rk.setVr05(rs.getString("vr05"));
								rk.setVr06(rs.getBigDecimal("vr06"));
								uvr06 = uvr06.add(rs.getBigDecimal("vr06"));
							}

							//uvr03 = uvr03+((BigDecimal)((int)rs.getBigDecimal("vr03")*100.0))/100.0;


							rk.setVr07(rs.getBigDecimal("vr07"));
							uvr07 = uvr07.add(rs.getBigDecimal("vr07"));


							rk.setVr08(rs.getBigDecimal("vr08"));
							uvr08 = uvr08.add(rs.getBigDecimal("vr08"));

							rk.setVr09(rs.getBigDecimal("vr09"));
							uvr09 = uvr09.add(rs.getBigDecimal("vr09"));

							rk.setVr10(rs.getBigDecimal("vr10"));
							uvr10 = uvr10.add(rs.getBigDecimal("vr10"));

							rk.setVr11(rs.getBigDecimal("vr11"));
							uvr11 = uvr11.add(rs.getBigDecimal("vr11"));

							rk.setVr12(rs.getBigDecimal("vr12"));
							uvr12 = uvr12.add(rs.getBigDecimal("vr12"));

							rk.setVr13(rs.getBigDecimal("vr13"));
							uvr13 = uvr13.add(rs.getBigDecimal("vr13"));

							rk.setVr14(rs.getBigDecimal("vr14"));
							uvr14 = uvr14.add(rs.getBigDecimal("vr14"));

							rk.setVr15(rs.getBigDecimal("vr15"));
							uvr15 = uvr15.add(rs.getBigDecimal("vr15"));

							rk.setVr16(rs.getBigDecimal("vr16"));
							uvr16 = uvr16.add(rs.getBigDecimal("vr16"));

							rk.setVr17(rs.getBigDecimal("vr17"));
							uvr17 = uvr17.add(rs.getBigDecimal("vr17"));

							if(Integer.parseInt(danString)<Integer.parseInt(userData.get("zaposlenik_dan_unosa_prvi").toString()) || Integer.parseInt(danString)>Integer.parseInt(userData.get("zaposlenik_dan_unosa_zadnji").toString()))
							{
								System.out.println();
								rk.setVr18(new BigDecimal ("0.00"));
								uvr18 = uvr18.add(new BigDecimal ("0.00"));
								System.out.println(rk.getVr08()+" --- "+uvr08);
							}
							else{
							rk.setVr18(rs.getBigDecimal("vr18"));
							uvr18 = uvr18.add(rs.getBigDecimal("vr18"));
							}
							rk.setVr19(rs.getBigDecimal("vr19"));
							uvr19 = uvr19.add(rs.getBigDecimal("vr19"));

							rk.setVr20(rs.getString("vr20"));
							//uvr20 = uvr20.add(rs.getBigDecimal("vr20"));

							rk.setVr21(rs.getBigDecimal("vr21"));
							uvr21 = uvr21.add(rs.getBigDecimal("vr21"));

							/*rk.setVr48(rs.getBigDecimal("vr48"));
							uvr48 = uvr48.add(rs.getBigDecimal("vr48"));*/

							rk.setVr22(rs.getBigDecimal("vr22"));
							uvr22 = uvr22.add(rs.getBigDecimal("vr22"));

							rk.setVr23(rs.getBigDecimal("vr23"));
							uvr23 = uvr23.add(rs.getBigDecimal("vr23"));

							rk.setVr24(rs.getBigDecimal("vr24"));
							uvr24 = uvr24.add(rs.getBigDecimal("vr24"));

							rk.setVr25(rs.getBigDecimal("vr25"));
							uvr25 = uvr25.add(rs.getBigDecimal("vr25"));

							rk.setVr26(rs.getBigDecimal("vr26"));
							uvr26 = uvr26.add(rs.getBigDecimal("vr26"));

							rk.setVr27(rs.getBigDecimal("vr27"));
							uvr27 = uvr27.add(rs.getBigDecimal("vr27"));

							rk.setVr28(rs.getBigDecimal("vr28"));
							rk.setVr29(rs.getBigDecimal("vr29"));
							rk.setVr30(rs.getBigDecimal("vr30"));
							rk.setVr31(rs.getBigDecimal("vr31"));

							rk.setVr32(rs.getBigDecimal("vr32"));
							rk.setVr33(rs.getBigDecimal("vr33"));
							rk.setVr34(rs.getBigDecimal("vr34"));
							rk.setVr35(rs.getBigDecimal("vr35"));
							rk.setVr36(rs.getBigDecimal("vr36"));
							rk.setVr37(rs.getBigDecimal("vr37"));
							rk.setVr38(rs.getBigDecimal("vr38"));
							rk.setVr39(rs.getBigDecimal("vr39"));
							rk.setVr40(rs.getBigDecimal("vr40"));
							rk.setVr41(rs.getBigDecimal("vr41"));
							rk.setVr42(rs.getBigDecimal("vr42"));
							rk.setVr43(rs.getBigDecimal("vr43"));
							rk.setVr44(rs.getBigDecimal("vr44"));
							rk.setVr45(rs.getBigDecimal("vr45"));
							rk.setVr46(rs.getBigDecimal("vr46"));
							rk.setVr47(rs.getString("vr47"));

							if (isWeekend)
								{
									rk.setVikendNeradniDan(true);
									//System.out.println(" praznik :"+rk.isVikendNeradniDan());
								}
							else if(isHoliday)
								{
									rk.setVikendNeradniDan(true);
								}
							rk.setUnesenoUBazu(true);
							j++;
						}
					else
						{
							System.out.println("Terenski radnik : "+userData.get(TRANS_TERENSKIRADNIK));
							//System.out.println("----Izvan RS-a : i= "+i+", dan u Tjednu :"+danUTjednu+" ----");
							//rk = new RadniKalendar();
							rk = new RadniKalendarNew();
							//rk.setIDzaposlenikUnos("169087");
							rk.setDatumUnosa(danUTjednu);
							//rk.setDatumUnosa("00");
							rk.setDanUTjednu(nazivDanaHR);
							//rk.setDanUTjednu("p");
							System.out.println("UUUUUUUUUUUUU : "+sifraOrgJed.subSequence(0, 1));

							System.out.println("ORGANIZACIJSKA JEDINICA : "+sifraOrgJed);
							System.out.println("sdadasds ... "+isDay.getActualMaximum(Calendar.DAY_OF_MONTH));


							if(userData.get("zaposlenik_dan_unosa_prvi")==null)
							{
								userData.put("zaposlenik_dan_unosa_prvi", 1);
							}
							if(userData.get("zaposlenik_dan_unosa_zadnji")==null){
									userData.put("zaposlenik_dan_unosa_zadnji", isDay.getActualMaximum(Calendar.DAY_OF_MONTH));
							}

							if(sifraOrgJed.subSequence(0, 1).equals("V"))
								{
								System.out.println(Integer.parseInt(userData.get("zaposlenik_dan_unosa_prvi").toString())+" ---------------------------  "+danString+"  -------------------------------------- VVVVVVVVVVVVVVVVVVVVVVVV"+Integer.parseInt(userData.get("zaposlenik_dan_unosa_zadnji").toString()));
								if(Integer.parseInt(danString)<Integer.parseInt(userData.get("zaposlenik_dan_unosa_prvi").toString()) || Integer.parseInt(danString)>Integer.parseInt(userData.get("zaposlenik_dan_unosa_zadnji").toString()))
								{
									System.out.println("IIIIIIIIIIIIFFFFFFFFFFFFFFFFFFFF");
									rk.setVr01("");
									rk.setVr02("");
									rk.setVr04("");
									rk.setVr05("");
									System.out.println("1 "+userData.get(TRANS_TERENSKIRADNIK));
									if(userData.get(TRANS_TERENSKIRADNIK).toString().equals("true")&& isWeekend==false)
									{
										if(isHoliday==false)
										{
										rk.setVr20("");
										}
									}

									}
								else{
									System.out.println("EEEEEEELLLLLLLLLLSSSSSSSSSEEEEEEEEEEEE");
									rk.setVr01("07:00");
									rk.setVr02("15:00");
									rk.setVr04("07:00");
									rk.setVr05("15:00");
									System.out.println("1 "+userData.get(TRANS_TERENSKIRADNIK));
									if(userData.get(TRANS_TERENSKIRADNIK).toString().equals("true")&& isWeekend==false)
									{
										if(isHoliday==false)
										{
										rk.setVr20("T37");
										}
									}


								}
									//System.out.println("----------- 1");
								}
							else if (sifraOrgJed.subSequence(0, 1).equals("0"))
								{
								if(sifraOrgJed.subSequence(0, 5).equals("00002"))
									{
									if(Integer.parseInt(danString)<Integer.parseInt(userData.get("zaposlenik_dan_unosa_prvi").toString()) || Integer.parseInt(danString)>Integer.parseInt(userData.get("zaposlenik_dan_unosa_zadnji").toString()))
									{
									System.out.println("2 "+userData.get(TRANS_TERENSKIRADNIK));
										rk.setVr01("");
										rk.setVr02("");
										rk.setVr04("");
										rk.setVr05("");
										if(userData.get(TRANS_TERENSKIRADNIK).toString().equals("true")&& isWeekend==false)
										{
											if(isHoliday==false)
											{
											rk.setVr20("");
											}
										}

										}
									else{

										System.out.println("2 "+userData.get(TRANS_TERENSKIRADNIK));
											rk.setVr01("07:00");
											rk.setVr02("15:00");
											rk.setVr04("07:00");
											rk.setVr05("15:00");
											if(userData.get(TRANS_TERENSKIRADNIK).toString().equals("true")&& isWeekend==false)
											{
												if(isHoliday==false)
												{
												rk.setVr20("T37");
												}
											}


									}
										//System.out.println("----------------- 2");
									}
								else
									{
									if(Integer.parseInt(danString)<Integer.parseInt(userData.get("zaposlenik_dan_unosa_prvi").toString()) || Integer.parseInt(danString)>Integer.parseInt(userData.get("zaposlenik_dan_unosa_zadnji").toString()))
									{
									System.out.println("3 "+userData.get(TRANS_TERENSKIRADNIK));
										rk.setVr01("");//izmjena s 08 na 07 zbog zahtijeva korisnika
										rk.setVr02("");//izmjena s 16 na 15 zbog zahtijeva korisnika
										rk.setVr04("");//izmjena s 08 na 07 zbog zahtijeva korisnika
										rk.setVr05("");//izmjena s 16 na 15 zbog zahtijeva korisnika
										if(userData.get(TRANS_TERENSKIRADNIK).toString().equals("true")&& isWeekend==false)
										{
											if(isHoliday==false)
											{
											rk.setVr20("");
											}
										}
									}
									else{

										System.out.println("3 "+userData.get(TRANS_TERENSKIRADNIK));
											rk.setVr01("07:30");//izmjena s 08 na 07 zbog zahtijeva korisnika
											rk.setVr02("15:30");//izmjena s 16 na 15 zbog zahtijeva korisnika
											rk.setVr04("07:30");//izmjena s 08 na 07 zbog zahtijeva korisnika
											rk.setVr05("15:30");//izmjena s 16 na 15 zbog zahtijeva korisnika
											if(userData.get(TRANS_TERENSKIRADNIK).toString().equals("true")&& isWeekend==false)
											{
												if(isHoliday==false)
												{
												rk.setVr20("T37");
												}
											}

									}

									}
								}
							else
								{
								if(Integer.parseInt(danString)<Integer.parseInt(userData.get("zaposlenik_dan_unosa_prvi").toString()) || Integer.parseInt(danString)>Integer.parseInt(userData.get("zaposlenik_dan_unosa_zadnji").toString()))
								{
								System.out.println("4 "+userData.get(TRANS_TERENSKIRADNIK));
									rk.setVr01("");
									rk.setVr02("");
									rk.setVr04("");
									rk.setVr05("");
									//System.out.println("------------- 3");
									if(userData.get(TRANS_TERENSKIRADNIK).toString().equals("true")&& isWeekend==false)
									{
										if(isHoliday==false)
										{
										rk.setVr20("");
										}
									}
								}
								else{

									System.out.println("4 "+userData.get(TRANS_TERENSKIRADNIK));
										rk.setVr01("");
										rk.setVr02("");
										rk.setVr04("00:00");
										rk.setVr05("00:00");
										//System.out.println("------------- 3");
										if(userData.get(TRANS_TERENSKIRADNIK).toString().equals("true")&& isWeekend==false)
										{
											if(isHoliday==false)
											{
											rk.setVr20("T37");
											}
										}

								}
								}

							//rk.setVr03("--");

							if (isWeekend)
								{
									rk.setVr01("");
									rk.setVr02("");
									rk.setVikendNeradniDan(true);
									//rk.setVr05(null);
									rk.setVr03(new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP));
								}
							else if(isHoliday)
								{

									rk.setVr01("");
									rk.setVr02("");
									rk.setVikendNeradniDan(true);
									//rk.setVr05(null);
									if(Integer.parseInt(danString)<Integer.parseInt(userData.get("zaposlenik_dan_unosa_prvi").toString()) || Integer.parseInt(danString)>Integer.parseInt(userData.get("zaposlenik_dan_unosa_zadnji").toString()))
									{
									rk.setVr18(new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP));
									rk.setVr03(new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP));
									}
									else{
										rk.setVr18(new BigDecimal(8.00).setScale(2, BigDecimal.ROUND_UP));
										rk.setVr03(new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP));
									}
								}
							else
								{
								if(Integer.parseInt(danString)<Integer.parseInt(userData.get("zaposlenik_dan_unosa_prvi").toString()) || Integer.parseInt(danString)>Integer.parseInt(userData.get("zaposlenik_dan_unosa_zadnji").toString()))
								{
									if(sifraOrgJed.subSequence(0, 1).equals("V"))
										{
										//System.out.println("userd :"+ userData.get(TRANS_UGOVOR_ZAPOSLENIKA));
											if(sifraOrgJed.equals("V100") || userData.get(TRANS_UGOVOR_ZAPOSLENIKA).toString().equals("2"))
												{
												System.out.println("5 "+userData.get(TRANS_TERENSKIRADNIK));
													rk.setVr01("");//izmjena s 08 na 07 zbog zahtijeva korisnika
													rk.setVr02("");//izmjena s 16 na 15 zbog zahtijeva korisnika
													rk.setVr04("");//izmjena s 08 na 07 zbog zahtijeva korisnika
													rk.setVr05("");//izmjena s 16 na 15 zbog zahtijeva korisnika
													if(userData.get(TRANS_TERENSKIRADNIK).toString().equals("true")&& isWeekend==false)
													{
														if(isHoliday==false)
														{
														rk.setVr20("");
														}
													}
												}
											else
												{
												System.out.println("6 "+userData.get(TRANS_TERENSKIRADNIK));
													rk.setVr01("");
													rk.setVr02("");
													rk.setVr04("");
													rk.setVr05("");
													if(userData.get(TRANS_TERENSKIRADNIK).toString().equals("true")&& isWeekend==false)
													{
														if(isHoliday==false)
														{
														rk.setVr20("");
														}
													}
												}
										}
									else if(sifraOrgJed.subSequence(0, 1).equals("0"))
									{
										if(sifraOrgJed.subSequence(0, 5).equals("00002"))
											{
											System.out.println("7 "+userData.get(TRANS_TERENSKIRADNIK));
												rk.setVr01("");
												rk.setVr02("");
												rk.setVr04("");
												rk.setVr05("");
												if(userData.get(TRANS_TERENSKIRADNIK).toString().equals("true")&& isWeekend==false)
												{
													if(isHoliday==false)
													{
													rk.setVr20("");
													}
												}
											}
										else
											{
											System.out.println("8 "+userData.get(TRANS_TERENSKIRADNIK));
											rk.setVr01("");//izmjena s 08 na 07 zbog zahtijeva korisnika
											rk.setVr02("");//izmjena s 16 na 15 zbog zahtijeva korisnika
											rk.setVr04("");//izmjena s 08 na 07 zbog zahtijeva korisnika
											rk.setVr05("");//izmjena s 16 na 15 zbog zahtijeva korisnika
											if(userData.get(TRANS_TERENSKIRADNIK).toString().equals("true")&& isWeekend==false)
											{
												if(isHoliday==false)
												{
												rk.setVr20("");
												}
											}
											}
									}
									else
										{
										System.out.println("9 "+userData.get(TRANS_TERENSKIRADNIK));
											rk.setVr01("");//izmjena s 08 na 07 zbog zahtijeva korisnika
											rk.setVr02("");//izmjena s 16 na 15 zbog zahtijeva korisnika
											rk.setVr04("");//izmjena s 08 na 07 zbog zahtijeva korisnika
											rk.setVr05("");//izmjena s 16 na 15 zbog zahtijeva korisnika
											if(userData.get(TRANS_TERENSKIRADNIK).toString().equals("true")&& isWeekend==false)
											{
												if(isHoliday==false)
												{
												rk.setVr20("");
												}
											}
										}
									//rk.setVr05(8.00);
									rk.setVr03(new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP));
									rk.setVr06(new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_UP));
								}
								else{

									if(sifraOrgJed.subSequence(0, 1).equals("V"))
										{
										//System.out.println("userd :"+ userData.get(TRANS_UGOVOR_ZAPOSLENIKA));
											if(sifraOrgJed.equals("V100") || userData.get(TRANS_UGOVOR_ZAPOSLENIKA).toString().equals("2"))
												{
												System.out.println("5 "+userData.get(TRANS_TERENSKIRADNIK));
													rk.setVr01("07:30");//izmjena s 08 na 07 zbog zahtijeva korisnika
													rk.setVr02("15:30");//izmjena s 16 na 15 zbog zahtijeva korisnika
													rk.setVr04("07:30");//izmjena s 08 na 07 zbog zahtijeva korisnika
													rk.setVr05("15:30");//izmjena s 16 na 15 zbog zahtijeva korisnika
													if(userData.get(TRANS_TERENSKIRADNIK).toString().equals("true")&& isWeekend==false)
													{
														if(isHoliday==false)
														{
														rk.setVr20("T37");
														}
													}
												}
											else
												{
												System.out.println("6 "+userData.get(TRANS_TERENSKIRADNIK));
													rk.setVr01("07:00");
													rk.setVr02("15:00");
													rk.setVr04("07:00");
													rk.setVr05("15:00");
													if(userData.get(TRANS_TERENSKIRADNIK).toString().equals("true")&& isWeekend==false)
													{
														if(isHoliday==false)
														{
														rk.setVr20("T37");
														}
													}
												}
										}
									else if(sifraOrgJed.subSequence(0, 1).equals("0"))
									{
										if(sifraOrgJed.subSequence(0, 5).equals("00002"))
											{
											System.out.println("7 "+userData.get(TRANS_TERENSKIRADNIK));
												rk.setVr01("07:00");
												rk.setVr02("15:00");
												rk.setVr04("07:00");
												rk.setVr05("15:00");
												if(userData.get(TRANS_TERENSKIRADNIK).toString().equals("true")&& isWeekend==false)
												{
													if(isHoliday==false)
													{
													rk.setVr20("T37");
													}
												}
											}
										else
											{
											System.out.println("8 "+userData.get(TRANS_TERENSKIRADNIK));
											rk.setVr01("07:30");//izmjena s 08 na 07 zbog zahtijeva korisnika
											rk.setVr02("15:30");//izmjena s 16 na 15 zbog zahtijeva korisnika
											rk.setVr04("07:30");//izmjena s 08 na 07 zbog zahtijeva korisnika
											rk.setVr05("15:30");//izmjena s 16 na 15 zbog zahtijeva korisnika
											if(userData.get(TRANS_TERENSKIRADNIK).toString().equals("true")&& isWeekend==false)
											{
												if(isHoliday==false)
												{
												rk.setVr20("T37");
												}
											}
											}
									}
									else
										{
										System.out.println("9 "+userData.get(TRANS_TERENSKIRADNIK));
											rk.setVr01("07:30");//izmjena s 08 na 07 zbog zahtijeva korisnika
											rk.setVr02("15:30");//izmjena s 16 na 15 zbog zahtijeva korisnika
											rk.setVr04("07:30");//izmjena s 08 na 07 zbog zahtijeva korisnika
											rk.setVr05("15:30");//izmjena s 16 na 15 zbog zahtijeva korisnika
											if(userData.get(TRANS_TERENSKIRADNIK).toString().equals("true")&& isWeekend==false)
											{
												if(isHoliday==false)
												{
												rk.setVr20("T37");
												}
											}
										}
									//rk.setVr05(8.00);
									rk.setVr03(new BigDecimal(8.00).setScale(2, BigDecimal.ROUND_UP));
									rk.setVr06(new BigDecimal(8.00).setScale(2, BigDecimal.ROUND_UP));

									}
								}

							rk.setUnesenoUBazu(false);
							//System.out.println(" praznik :"+rk.isVikendNeradniDan());
							//System.out.println("i ==== "+i+" --- "+rs.absolute(j));

						}

					//j=i-1;s
					lZRK.add(rk);
					lUkupnoSati.add(us);
					isDay.clear();
		//<sx<sx>
				}

			//System.out.println("-------------- Lista ukupno sati------------");
			//String vrijednostiVRUkupno = "vr04:"+uvr04+"|vr05:"+uvr05+"|vr06:"+uvr06+"|vr07:"+uvr07+"|vr09:"+uvr09+"|vr10:"+uvr10+"|vr14:"+uvr14+"|vr15:"+uvr15+"|vr20:"+uvr20+"|vr24:"+uvr24+"|vr25:"+uvr25+"|vr26:"+uvr26+"|vr32:"+uvr32+"|vr40:"+uvr40+"|vr41:"+uvr41+"|vr42:"+uvr42+"|vr43:"+uvr43+"|vr44:"+uvr44+"|vr45:"+uvr45+"|vr46:"+uvr46;
			String vrijednostiVRUkupno = "vr03:"+uvr03+"|vr06:"+uvr06+"|vr07:"+uvr07+"|vr08:"+uvr08+"|vr09:"+uvr09+"|vr10:"+uvr10+"|vr11:"+uvr11+"|vr12:"+uvr12+"|vr13:"+uvr13+"|vr14:"+uvr14+"|vr15:"+uvr15+"|vr16:"+uvr16+"|vr17:"+uvr17+"|vr18:"+uvr18+"|vr19:"+uvr19+"|vr21:"+uvr21+"|vr22:"+uvr22+"|vr23:"+uvr23+"|vr24:"+uvr24+"|vr25:"+uvr25+"|vr26:"+uvr26+"|vr27:"+uvr27;
			System.out.println("----------------------------------------Ivan--------------------------------------" +uvr24);
			System.out.println(vrijednostiVRUkupno);
			userData.put(TRANS_STRING_UKUPNE_VRIJEDNOSTI, vrijednostiVRUkupno);

			//System.out.println("ukupno vrijeme provedeno na poslu = "+uvr05);


		} catch (Exception e) {
		// TODO: handle exception
			System.out.println(e.getMessage());
		}
		finally {
		if (trx != null)
			trx.closeIfPossible();
			lUkupnoSati.clear();
		}
		return lZRK;

}

	private int calculateDifference(java.sql.Date periodOd, java.sql.Date periodDo)
	{
		int tempDifference=0;
		int difference = 0;
		Calendar earlier = Calendar.getInstance();
		Calendar later = Calendar.getInstance();

		if(periodOd.compareTo(periodDo)<0)
		{
			earlier.setTime(periodOd);
			later.setTime(periodDo);
		}
		 else
		    {
		        earlier.setTime(periodDo);
		        later.setTime(periodOd);
		    }

		    while (earlier.get(Calendar.YEAR) != later.get(Calendar.YEAR))
		    {
		        tempDifference = 365 * (later.get(Calendar.YEAR) - earlier.get(Calendar.YEAR));
		        difference += tempDifference;

		        earlier.add(Calendar.DAY_OF_YEAR, tempDifference);
		    }

		    if (earlier.get(Calendar.DAY_OF_YEAR) != later.get(Calendar.DAY_OF_YEAR))
		    {
		        tempDifference = later.get(Calendar.DAY_OF_YEAR) - earlier.get(Calendar.DAY_OF_YEAR);
		        difference += tempDifference;

		        earlier.add(Calendar.DAY_OF_YEAR, tempDifference);
		    }

		System.out.println("---------00000000------"+difference);
		return difference;
	}

	private String danUTjednu(String datum) throws BaseFrameworkException
	{
		//System.out.println(" DATUM U RADNOM KALENDARU : "+datum);
		Calendar cal = Calendar.getInstance();
		Integer dan = Integer.parseInt(datum.substring(0, 2));
		Integer mjesec = Integer.parseInt(datum.substring(3, 5));
		Integer godina = Integer.parseInt(datum.substring(6,10));
		Integer danUTjednu = 0;
		String nazivDana = null;
		String nazivDanaSkr = null;
		danUTjednu = UtilDate.lastDayOfMonthInt(godina, mjesec);
		cal.set(godina, mjesec-1, dan);
		String dayNames[] = new DateFormatSymbols().getWeekdays();
	      nazivDana = dayNames[cal.get(Calendar.DAY_OF_WEEK)];
	      //dc

		//System.out.println("Parsirani datum je :"+nazivDana.substring(0, 3)+" / "+mjesec+" / "+godina);

		nazivDanaSkr = nazivDana.substring(0, 3);

		if(nazivDanaSkr.equals("Mon")){nazivDanaSkr = "pon";}
		else if(nazivDanaSkr.equals("Tue")){nazivDanaSkr="uto";}
		else if(nazivDanaSkr.equals("Wed")){nazivDanaSkr="sri";}
		else if(nazivDanaSkr.equals("Thu")){nazivDanaSkr="čet";}
		else if(nazivDanaSkr.equals("Fri")){nazivDanaSkr="pet";}
		else if(nazivDanaSkr.equals("Sat")){nazivDanaSkr="sub";}
		else if(nazivDanaSkr.equals("Sun")){nazivDanaSkr="ned";}
		else{}

		return nazivDanaSkr;
	}

}
