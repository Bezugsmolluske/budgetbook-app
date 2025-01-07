import 'package:flutter/material.dart';
import '../settings_controller.dart';

class ThemeDropdown extends StatelessWidget {
  const ThemeDropdown({
    super.key,
    required this.controller,
  });

  final SettingsController controller;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.teal.shade700,
        borderRadius: BorderRadius.circular(10),
        boxShadow: [
          BoxShadow(
            color: Colors.black26,
            blurRadius: 4,
            offset: Offset(0, 2),
          ),
        ],
      ),
      padding: EdgeInsets.symmetric(horizontal: 16),
      child: DropdownButton<ThemeMode>(
        value: controller.themeMode,
        onChanged: controller.updateThemeMode,
        items: const [
          DropdownMenuItem(
            value: ThemeMode.system,
            child: Text('System Theme'),
          ),
          DropdownMenuItem(
            value: ThemeMode.light,
            child: Text('Light Theme'),
          ),
          DropdownMenuItem(
            value: ThemeMode.dark,
            child: Text('Dark Theme'),
          )
        ],
        dropdownColor: Colors.teal.shade700,
        style: TextStyle(color: Colors.white),
        isExpanded: true,
      ),
    );
  }
}
