import 'package:flutter/material.dart';
import 'settings_controller.dart';
import 'widgets/theme_dropdown.dart';

class SettingsView extends StatelessWidget {
  const SettingsView({super.key, required this.controller});

  static const routeName = '/settings';

  final SettingsController controller;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Einstellungen'),
        backgroundColor: Colors.teal,
      ),
      body: Center(
        child: ConstrainedBox(
          constraints: BoxConstraints(maxWidth: 800),
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Theme',
                  style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
                ),
                SizedBox(height: 16),
                ThemeDropdown(controller: controller),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
