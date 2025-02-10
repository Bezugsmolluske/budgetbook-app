import 'package:budgetbook_app/src/expenses/expenses_view.dart';
import 'package:budgetbook_app/src/incomes/incomes_view.dart';
import 'package:budgetbook_app/src/settings/settings_view.dart';
import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

import 'settings/settings_controller.dart';
import 'overview/overview_view.dart';

class MyApp extends StatelessWidget {
  const MyApp({
    super.key,
    required this.settingsController,
  });

  final SettingsController settingsController;

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: settingsController,
      builder: (BuildContext context, Widget? child) {
        return MaterialApp(
          restorationScopeId: 'app',
          localizationsDelegates: const [
            AppLocalizations.delegate,
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          supportedLocales: const [Locale('en', ''), Locale('de', '')],
          onGenerateTitle: (BuildContext context) =>
              AppLocalizations.of(context)!.appTitle,
          theme: ThemeData(
            useMaterial3: true,
            colorScheme: ColorScheme.fromSeed(seedColor: Color(0xFF00FF00)),
          ),
          darkTheme: ThemeData.dark(),
          themeMode: settingsController.themeMode,
          home: AppWidget(settingsController: settingsController),
        );
      },
    );
  }
}

class AppWidget extends StatefulWidget {
  const AppWidget({
    super.key,
    required this.settingsController,
  });

  final SettingsController settingsController;

  @override
  State<AppWidget> createState() => _AppWidgetState();
}

class _AppWidgetState extends State<AppWidget> {
  var selectedIndex = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      bottomNavigationBar: NavigationBar(
        destinations: [
          NavigationDestination(
              icon: Icon(Icons.home_rounded), label: 'Übersicht'),
          NavigationDestination(
              icon: Icon(Icons.add_circle_outline_rounded),
              label: 'Einkommmen'),
          NavigationDestination(
              icon: Icon(Icons.remove_circle_outline_rounded),
              label: 'Ausgaben'),
          NavigationDestination(
              icon: Icon(Icons.settings_rounded), label: 'Einstellungen'),
        ],
        labelBehavior: NavigationDestinationLabelBehavior.onlyShowSelected,
        selectedIndex: selectedIndex,
        onDestinationSelected: (value) {
          setState(() {
            selectedIndex = value;
          });
        },
      ),
      body: SafeArea(
        child: [
          OverviewView(),
          IncomesView(),
          ExpensesView(),
          SettingsView(controller: widget.settingsController)
        ][selectedIndex],
      ),
    );
  }
}
